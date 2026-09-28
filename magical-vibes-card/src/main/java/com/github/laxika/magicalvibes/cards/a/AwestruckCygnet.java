package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IntensifyNamedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIntensityThreshold;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBLB", collectorNumber = "1")
public class AwestruckCygnet extends Card {

    private static final String CARD_NAME = "Awestruck Cygnet";
    private static final SourceIntensityThreshold TRANSFORM_THRESHOLD = new SourceIntensityThreshold(3);

    public AwestruckCygnet() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(Keyword.FLYING),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                        new ConditionalEffect(new NotCondition(TRANSFORM_THRESHOLD),
                                new IntensifyNamedCardsEffect(CARD_NAME))));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(TRANSFORM_THRESHOLD,
                new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(TRANSFORM_THRESHOLD,
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.VIGILANCE), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(TRANSFORM_THRESHOLD,
                new SetNameEffect("Radiant Swan", GrantScope.SELF)));
    }
}
