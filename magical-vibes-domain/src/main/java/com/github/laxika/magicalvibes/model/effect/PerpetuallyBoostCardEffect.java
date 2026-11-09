package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Applies a permanent power/toughness modification to a physical card identity: {@code card}, or with a {@code null}
 * card the card of the stack entry's triggering permanent as the ability triggered ("that Vehicle perpetually gets
 * +1/+0", Tiana, Angelic Mechanic; {@link #ofTriggeringPermanent}).
 */
public record PerpetuallyBoostCardEffect(Card card, DynamicAmount powerBoost, DynamicAmount toughnessBoost)
        implements CardEffect {

    /**
     * "That [permanent] perpetually gets +P/+T": the triggering permanent's card; with no triggering permanent, the
     * stack entry's triggering card ({@link #ofTriggeringCard}). The same record as {@link #ofTriggeringCard}.
     */
    public static PerpetuallyBoostCardEffect ofTriggeringPermanent(int powerBoost, int toughnessBoost) {
        return new PerpetuallyBoostCardEffect(null, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    /**
     * "It perpetually gets +P/+T" of a card the trigger event was about ("whenever you draw a Dragon card"): the stack
     * entry's triggering card, the card as the ability triggered. The same record as {@link #ofTriggeringPermanent}.
     */
    public static PerpetuallyBoostCardEffect ofTriggeringCard(int powerBoost, int toughnessBoost) {
        return ofTriggeringPermanent(powerBoost, toughnessBoost);
    }

    public PerpetuallyBoostCardEffect(Card card, int powerBoost, int toughnessBoost) {
        this(card, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }
}
