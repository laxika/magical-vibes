package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "12")
public class NecklaceOfGirion extends Card {

    public NecklaceOfGirion() {
        // Both abilities target a creature you control: one target group, each ability choosing its own target.
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addTrigger(Triggers.enters(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.LAND,
                                        new PermanentHasSubtypePredicate(CardSubtype.FOREST))),
                                ObjectRelation.YOU_CONTROL),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
