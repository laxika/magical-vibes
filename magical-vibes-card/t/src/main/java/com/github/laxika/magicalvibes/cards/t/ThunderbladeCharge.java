package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CastSourceCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;

@CardRegistration(set = "FUT", collectorNumber = "124")
public class ThunderbladeCharge extends Card {

    public ThunderbladeCharge() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new ConditionalEffect(
                                new SourceCardInGraveyard(),
                                new MayPayManaEffect(
                                        "{2}{R}{R}{R}",
                                        new CastSourceCardFromGraveyardWithoutPayingManaCostEffect(),
                                        "Pay {2}{R}{R}{R} to cast Thunderblade Charge without paying its mana cost?")),
                        false,
                        true));
    }
}
