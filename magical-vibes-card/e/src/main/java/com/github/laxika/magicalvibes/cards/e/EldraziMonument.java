package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "199")
@CardRegistration(set = "SLD", collectorNumber = "603")
@CardRegistration(set = "MB1", collectorNumber = "216")
@CardRegistration(set = "C15", collectorNumber = "253")
@CardRegistration(set = "M3C", collectorNumber = "290")
@CardRegistration(set = "CMA", collectorNumber = "216")
public class EldraziMonument extends Card {

    public EldraziMonument() {
        addStaticEffect(new StaticBoostEffect(1, 1,
                Set.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE), Subjects.creaturesYouControl()));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                new ConditionalEffect(new NotCondition(HappenedThisWay.sacrificed()),
                        new SacrificeSelfEffect())));
    }
}
