package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "141")
public class GlintHornBuccaneer extends Card {

    public GlintHornBuccaneer() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect()),
                "{1}{R}, Discard a card: Draw a card. Activate only if this creature is attacking.",
                ActivationTimingRestriction.ONLY_WHILE_ATTACKING
        ));
    }
}
