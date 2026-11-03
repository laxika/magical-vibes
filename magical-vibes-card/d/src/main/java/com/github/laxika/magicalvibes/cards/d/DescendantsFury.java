package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "736")
@CardRegistration(set = "CMM", collectorNumber = "766")
@CardRegistration(set = "ECC", collectorNumber = "93")
public class DescendantsFury extends Card {

    public DescendantsFury() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCreaturePredicate()),
                TriggerOptions.ONE_OR_MORE,
                new MayEffect(
                        new SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect(),
                        "Sacrifice one of them?"));
    }
}
