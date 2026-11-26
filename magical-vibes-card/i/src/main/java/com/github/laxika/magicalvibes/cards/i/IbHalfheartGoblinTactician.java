package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "163")
@CardRegistration(set = "DD1", collectorNumber = "43")
@CardRegistration(set = "EVG", collectorNumber = "43")
public class IbHalfheartGoblinTactician extends Card {

    public IbHalfheartGoblinTactician() {
        addTrigger(Triggers.creatureYouControlBecomesBlocked()
                        .withFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))),
                SequenceEffect.of(new SacrificeSelfEffect(Subjects.triggeringOrSource()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), DealDamageEffect.toEach(
                                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentBlockingSourcePredicate()),
                                4).byTriggeringPermanent())));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                        new CreateTokenEffect(
                                2, "Goblin", 1, 1, CardColor.RED,
                                List.of(CardSubtype.GOBLIN), Set.of(), Set.of())
                ),
                "Sacrifice two Mountains: Create two 1/1 red Goblin creature tokens."
        ));
    }
}
