package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement effect: while a source with this effect is on the battlefield, if its controller would draw
 * a card, they draw two cards instead (Thought Reflection; under a condition, Vnwxt, Verbose Host). With
 * {@code exceptFirstDrawStepDraw}, the first card they draw in each of their own draw steps isn't replaced ("except the
 * first one you draw in each of your draw steps": Alhammarret's Archive, Bard, King of Dale, Teferi's Ageless Insight).
 * Each draw is replaced on its own (CR 121.2).
 *
 * @param exceptFirstDrawStepDraw whether the first card drawn in each of the controller's draw steps is left alone
 */
public record DoubleDrawReplacementEffect(boolean exceptFirstDrawStepDraw) implements CardEffect, DoublingEffect {

    /** "If you would draw a card, draw two cards instead." */
    public DoubleDrawReplacementEffect() {
        this(false);
    }

    /** "If you would draw a card except the first one you draw in each of your draw steps, draw two cards instead." */
    public static DoubleDrawReplacementEffect exceptFirstDrawInEachOfYourDrawSteps() {
        return new DoubleDrawReplacementEffect(true);
    }
}
