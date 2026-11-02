package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "605")
public class OriginOfCaptainAmerica extends Card {

    public OriginOfCaptainAmerica() {
        // Chapter I: Put a +1/+1 counter on target creature you control. It gains first strike
        // and vigilance until end of turn.
        addTrigger(Triggers.chapter(1),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.chapter(1), new GrantKeywordEffect(
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE), GrantScope.TARGET));
        setSagaChapterTargetGroups(1, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));

        // Chapter II: Create a Sturdy Shield Equipment token.
        CreateTokenEffect sturdyShield = CreateTokenEffect.ofArtifactToken(
                1, "Sturdy Shield", List.of(CardSubtype.EQUIPMENT),
                List.of(new EquipActivatedAbility("{2}")));
        addTrigger(Triggers.chapter(2),
                sturdyShield.withTokenEffects(List.of(StaticAbility.of(new StaticBoostEffect(1, 2, GrantScope.EQUIPPED_CREATURE)))));

        // Chapter III: Tap up to one target creature and put a stun counter on it.
        addTrigger(Triggers.chapter(3), new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.chapter(3),
                new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));
    }
}
