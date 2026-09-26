package com.github.laxika.magicalvibes.model.effect;

/** Makes every permanent created earlier in this same resolution attacking. */
public record MakeCreatedPermanentsAttackingEffect(boolean tapped) implements CardEffect {

    public MakeCreatedPermanentsAttackingEffect() {
        this(false);
    }
}
