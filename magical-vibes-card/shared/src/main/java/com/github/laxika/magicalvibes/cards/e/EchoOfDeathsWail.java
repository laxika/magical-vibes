package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class EchoOfDeathsWail extends Card {

    private static final PermanentAllOfPredicate RAT_TOKEN = new PermanentAllOfPredicate(List.of(
            new PermanentIsTokenPredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.RAT)));

    private static final PermanentAllOfPredicate ANOTHER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    public EchoOfDeathsWail() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GainControlOfAllPermanentsMatchingEffect(RAT_TOKEN));
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, ANOTHER_CREATURE, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new DrawCardEffect(1))),
                "Sacrifice another creature?"));
    }
}
