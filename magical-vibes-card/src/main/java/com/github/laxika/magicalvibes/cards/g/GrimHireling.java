package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "970")
@CardRegistration(set = "SLD", collectorNumber = "2493")
public class GrimHireling extends Card {

    public GrimHireling() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, CreateTokenEffect.ofTreasureToken(2), false, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeXPermanentsCost(new PermanentHasSubtypePredicate(CardSubtype.TREASURE)),
                        new BoostTargetCreatureEffect(
                                new Scaled(new XValue(), -1),
                                new Scaled(new XValue(), -1))
                ),
                "{B}, Sacrifice X Treasures: Target creature gets -X/-X until end of turn. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
