package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "107")
public class BlastingStation extends Card {

    public BlastingStation() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "{T}, Sacrifice a creature: This artifact deals 1 damage to any target."
        ));

        addTrigger(Triggers.enters(null, ObjectRelation.ANY),
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new MayEffect(
                                new UntapPermanentsEffect(TapUntapScope.SELF),
                                "Untap Blasting Station?"
                        )));
    }
}
