package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CastSourceCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "124")
public class ThunderbladeCharge extends Card {

    public ThunderbladeCharge() {
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
        addTrigger(new TriggerCondition(Triggers.creatureYouControlDealsCombatDamageToPlayer(), AbilityZones.GRAVEYARD),
                TriggerOptions.ONE_OR_MORE.withInterveningIf(new SourceCardInGraveyard()),
                new MayPayManaEffect(
                        "{2}{R}{R}{R}",
                        new CastSourceCardFromGraveyardWithoutPayingManaCostEffect(),
                        "Pay {2}{R}{R}{R} to cast Thunderblade Charge without paying its mana cost?"));
    }
}
