package com.github.laxika.magicalvibes.model;

import java.util.List;

public record GameLogEntry(List<GameLogSegment> segments) {

    public GameLogEntry {
        segments = List.copyOf(segments);
    }

    public static GameLogEntry text(String text) {
        return new GameLogEntry(List.of(GameLogSegment.text(text)));
    }

    public String plainText() {
        StringBuilder sb = new StringBuilder();
        for (GameLogSegment segment : segments) {
            switch (segment) {
                case GameLogSegment.Text text -> sb.append(text.value());
                case GameLogSegment.CardSegment card -> sb.append(card.card().getName());
            }
        }
        return sb.toString();
    }
}
