package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsThenReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "153")
public class YotiaDeclaresWar extends Card {

    public YotiaDeclaresWar() {
        addStaticEffect(new ReadAheadEffect(false));

        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                1, "Ornithopter", 0, 2, null,
                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));

        addTrigger(Triggers.chapter(2), new TapAnyNumberOfPermanentsThenReflexiveAbilityEffect(
                new PermanentIsArtifactPredicate(),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(new EventValue())));

        addTrigger(Triggers.chapter(3), new AnimatePermanentsEffect(
                4, 4, List.of(), Set.of(), null, Set.of(), GrantScope.TARGET,
                EffectDuration.UNTIL_END_OF_TURN));
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(), "Target must be an artifact you control"), 0, 1)));
    }
}
