package com.github.laxika.magicalvibes.networking.model;

public record GameLogSegmentView(String type, String text, CardView card) {

    public static GameLogSegmentView text(String text) {
        return new GameLogSegmentView("text", text, null);
    }

    public static GameLogSegmentView card(CardView card) {
        return new GameLogSegmentView("card", null, card);
    }
}
