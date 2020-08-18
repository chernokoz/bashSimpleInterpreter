package com.chernokoz;

import java.util.ArrayList;

public class Parser {

    ArrayList<Token> tokenList;

    private final Environment env;

    public Parser(ArrayList<Token> tokenList, Environment env) {
        this.tokenList = tokenList;
        this.env = env;
    }

    public ArrayList<Command> parseSequence(ArrayList<Token> sequenceTokenList) {
        ArrayList<Command> result = new ArrayList<>();
        String currentCommand = null;
        ArrayList<String> args = new ArrayList<>();
        boolean doubleQuoteFlag = false;
        boolean singleQuoteFlag = false;
        Token token;
        String tokenValue;

        for (int i = 0; i < sequenceTokenList.size(); i++) {

            token = sequenceTokenList.get(i);
            tokenValue = token.getToken();

            if (tokenValue.equals("\"") && !singleQuoteFlag) {
                doubleQuoteFlag = !doubleQuoteFlag;
            }

            if (tokenValue.equals("'") && !doubleQuoteFlag) {
                singleQuoteFlag = !singleQuoteFlag;
            }

            if (tokenValue.equals("$")
                    && !singleQuoteFlag
                    && i < sequenceTokenList.size() - 1
                    && !(sequenceTokenList.get(i + 1) instanceof WhiteSpaceToken)) {
                Token identifier = sequenceTokenList.get(i+1);
                sequenceTokenList.remove(i);
                sequenceTokenList.remove(i);
                if (env.isVar(identifier.getToken())) {
                    String value = env.getVar(identifier.getToken());
                    sequenceTokenList.add(i, Token.createToken(value));
                } else {
                    sequenceTokenList.add(i, Token.createToken(""));
                }
            }
        }

        ArrayList<Token> unquotedSequenceTokenList = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        singleQuoteFlag = doubleQuoteFlag = false;
        boolean isQuoteToken;
        boolean isInQuote;
        boolean isInnerQuote;

        for (Token item : sequenceTokenList) {

            token = item;
            tokenValue = token.getToken();
            isInQuote = doubleQuoteFlag || singleQuoteFlag;
            isQuoteToken = tokenValue.equals("\"") || tokenValue.equals("'");
            isInnerQuote = singleQuoteFlag && tokenValue.equals("\"")
                    || doubleQuoteFlag && tokenValue.equals("'");

            if (isInQuote && !isQuoteToken || isInnerQuote) {
                currentWord.append(token.getToken());
                continue;
            }

            if (!isInQuote && !isQuoteToken) {
                unquotedSequenceTokenList.add(token);
                continue;
            }

            if (!isInQuote) {
                if (tokenValue.equals("'")) {
                    singleQuoteFlag = true;
                }
                if (tokenValue.equals("\"")) {
                    doubleQuoteFlag = true;
                }
                continue;
            }

            if (singleQuoteFlag) {
                unquotedSequenceTokenList.add(Token.createToken(currentWord.toString()));
                currentWord = new StringBuilder();
                singleQuoteFlag = false;
                continue;
            }

            unquotedSequenceTokenList.add(Token.createToken(currentWord.toString()));
            currentWord = new StringBuilder();
            doubleQuoteFlag = false;

        }

        sequenceTokenList = unquotedSequenceTokenList;

        for (int i = 0; i < sequenceTokenList.size(); i++) {
            token = sequenceTokenList.get(i);
            tokenValue = token.getToken();

            if (tokenValue.equals("\"") && !singleQuoteFlag) {
                doubleQuoteFlag = !doubleQuoteFlag;
            }

            if (token instanceof WhiteSpaceToken) {
                continue;
            }

            if (currentCommand == null) {
                if (i + 1 < sequenceTokenList.size()
                        && sequenceTokenList.get(i + 1).getToken().equals("=")) {
                    String value =(i + 2 < sequenceTokenList.size()) ? sequenceTokenList.get(i + 2).getToken() : "";
                    env.putVar(token.getToken(), value);
                    i += 2;
                    continue;
                } else if (token instanceof ReservedWordToken) {
                    currentCommand = token.getToken();
                    continue;
                }
            }

            if (token.getToken().equals("|")) {
                result.add(Command.createCommandInstance(currentCommand, args, false, env));
                args = new ArrayList<>();
                currentCommand = null;
                continue;
            }

            args.add(token.getToken());

        }

        if (currentCommand != null) {
            result.add(Command.createCommandInstance(currentCommand, args, true, env));
        }

        return result;
    }

    public ArrayList<ArrayList<Command>> run() {
        ArrayList<ArrayList<Command>> result = new ArrayList<>();
        ArrayList<Token> currentSequenceTokens = new ArrayList<>();

        boolean doubleQuoteFlag = false;
        boolean singleQuoteFlag = false;

        for (Token token : tokenList) {
            if (token.getToken().equals("\"")) {
                doubleQuoteFlag = !doubleQuoteFlag;
            }
            if (token.getToken().equals("'")) {
                singleQuoteFlag = !singleQuoteFlag;
            }
            if (!doubleQuoteFlag && !singleQuoteFlag && token.getToken().equals(";")) {
                result.add(parseSequence(currentSequenceTokens));
                currentSequenceTokens = new ArrayList<>();
                continue;
            }
            currentSequenceTokens.add(token);
        }

        if (!currentSequenceTokens.isEmpty()) {
            result.add(parseSequence(currentSequenceTokens));
        }

        return result;
    }
}
