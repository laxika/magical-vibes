package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "605")
public class OriginOfCaptainAmerica extends Card {

    public OriginOfCaptainAmerica() {
        // Chapter I: Put a +1/+1 counter on target creature you control. It gains first strike
        // and vigilance until end of turn.
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new GrantKeywordEffect(
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE), GrantScope.TARGET));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));

        // Chapter II: Create a Sturdy Shield Equipment token.
        CreateTokenEffect sturdyShield = CreateTokenEffect.ofArtifactToken(
                1, "Sturdy Shield", List.of(CardSubtype.EQUIPMENT),
                List.of(new EquipActivatedAbility("{2}")));
        addEffect(EffectSlot.SAGA_CHAPTER_II, sturdyShield.withTokenEffects(Map.of(
                EffectSlot.STATIC,
                new StaticBoostEffect(1, 2, GrantScope.EQUIPPED_CREATURE))));

        // Chapter III: Tap up to one target creature and put a stun counter on it.
        addEffect(EffectSlot.SAGA_CHAPTER_III, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));
    }
}
