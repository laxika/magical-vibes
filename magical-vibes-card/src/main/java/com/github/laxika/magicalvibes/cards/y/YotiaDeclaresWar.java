package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsThenReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "153")
public class YotiaDeclaresWar extends Card {

    public YotiaDeclaresWar() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                1, "Ornithopter", 0, 2, null,
                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new TapAnyNumberOfPermanentsThenReflexiveAbilityEffect(
                new PermanentIsArtifactPredicate(),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(new EventValue())));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new AnimatePermanentsEffect(
                4, 4, List.of(), Set.of(), null, Set.of(), GrantScope.TARGET,
                EffectDuration.UNTIL_END_OF_TURN));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(), "Target must be an artifact you control"), 0, 1)));
    }
}
