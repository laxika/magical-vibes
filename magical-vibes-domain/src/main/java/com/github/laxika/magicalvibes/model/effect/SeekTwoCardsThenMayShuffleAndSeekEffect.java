package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Seeks two cards, then optionally shuffles those exact cards back and seeks two more. */
public record SeekTwoCardsThenMayShuffleAndSeekEffect(List<Card> soughtCards) implements CardEffect {

    /** Creates the initial two-card Seek. */
    public SeekTwoCardsThenMayShuffleAndSeekEffect() {
        this(List.of());
    }

    public SeekTwoCardsThenMayShuffleAndSeekEffect {
        soughtCards = List.copyOf(soughtCards);
    }
}
