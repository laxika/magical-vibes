package com.github.laxika.magicalvibes.model;

import java.util.Objects;

public sealed interface GameLogSegment permits GameLogSegment.Text, GameLogSegment.CardSegment {

    record Text(String value) implements GameLogSegment {
        public Text {
            Objects.requireNonNull(value);
        }
    }

    record CardSegment(Card card) implements GameLogSegment {
        public CardSegment {
            Objects.requireNonNull(card);
        }
    }

    static GameLogSegment text(String value) {
        return new Text(value);
    }

    static GameLogSegment card(Card card) {
        return new CardSegment(card);
    }
}
