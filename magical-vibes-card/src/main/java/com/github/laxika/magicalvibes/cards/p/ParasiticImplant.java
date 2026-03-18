package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "67")
public class ParasiticImplant extends Card {

    public ParasiticImplant() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // At the beginning of your upkeep, enchanted creature's controller sacrifices it
                // and you create a 1/1 colorless Phyrexian Myr artifact creature token.
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeEnchantedCreatureAndCreateTokenEffect(
                new CreateCreatureTokenEffect(
                        "Phyrexian Myr",
                        1, 1,
                        null,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR),
                        Set.of(),
                        Set.of(CardType.ARTIFACT)
                )
        ));
    }
}
