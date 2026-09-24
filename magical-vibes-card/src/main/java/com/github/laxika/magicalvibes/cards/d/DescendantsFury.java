package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "CMM", collectorNumber = "736")
@CardRegistration(set = "CMM", collectorNumber = "766")
public class DescendantsFury extends Card {

    public DescendantsFury() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new MayEffect(
                                new SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect(),
                                "Sacrifice one of them?"),
                        false,
                        true));
    }
}
