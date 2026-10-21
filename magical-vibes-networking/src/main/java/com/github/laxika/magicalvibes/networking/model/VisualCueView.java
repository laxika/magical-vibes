package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.event.VisualCueType;

import java.util.List;
import java.util.UUID;

/**
 * Wire form of one cosmetic visual cue. The client plays the cues of a {@code GAME_STATE}
 * message, in order, on the board as it currently is, then applies the message's state.
 * Every field except {@code type} may be null; a missing source has no colors. {@code targetCardId}
 * is a spell on the stack; {@code counterType} is set only on {@code COUNTERS_ADDED}.
 */
public record VisualCueView(
        VisualCueType type,
        UUID sourceCardId,
        String sourceName,
        List<CardColor> sourceColors,
        UUID targetPermanentId,
        UUID targetPlayerId,
        UUID targetCardId,
        Integer amount,
        CounterType counterType) {

    public VisualCueView {
        sourceColors = List.copyOf(sourceColors);
    }
}
