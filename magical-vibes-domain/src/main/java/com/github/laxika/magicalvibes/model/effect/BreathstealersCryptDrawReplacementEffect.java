package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker for Breathstealer's Crypt: if a player would draw a card, instead they draw it and
 * reveal it; if it's a creature card, that player discards it unless they pay {@code lifeCost} life.
 * Detected in {@code DrawService.performDrawCard} for every player's draw; the pay-or-discard choice
 * is offered as a may-ability handled by {@code BreathstealersCryptDrawReplacementHandler}.
 *
 * @param lifeCost life the drawing player may pay to keep a revealed creature card (Crypt: 3)
 */
public record BreathstealersCryptDrawReplacementEffect(int lifeCost) implements CardEffect {

    public BreathstealersCryptDrawReplacementEffect() {
        this(3);
    }
}
