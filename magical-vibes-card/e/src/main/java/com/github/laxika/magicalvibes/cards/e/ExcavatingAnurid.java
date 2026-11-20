package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "163")
public class ExcavatingAnurid extends Card {

    public ExcavatingAnurid() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new DrawCardEffect(1))),
                "Sacrifice a land to draw a card?"));

        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE), Subjects.source())));
    }
}
