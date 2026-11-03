package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.FinalChapterTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SagaWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(SagaWatch.values(), LifeWatch.values(), TapWatch.values(), LibraryWatch.values(),
                        TargetWatch.values(), CounterWatch.values(), DamageWatch.values(), CombatWatch.values(),
                        SpellAbilityWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void everySagaWatchReadsAFinalChapterTriggerOnTheBattlefield() {
        assertThat(SagaWatch.values()).allSatisfy(watch -> {
            assertThat(watch.trigger()).as(watch.name()).isInstanceOf(FinalChapterTrigger.class);
            assertThat(watch.zone()).as(watch.name()).isEqualTo(Zone.BATTLEFIELD);
        });
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesOfItsTrigger() {
        Card card = new Card();
        DrawCardEffect resolves = new DrawCardEffect(1);
        GainLifeEffect chapter = new GainLifeEffect(2);
        card.addTrigger(Triggers.finalChapterAbilityResolves(), resolves);
        card.addTrigger(Triggers.chapter(3), chapter);

        assertThat(SagaWatch.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES.effectsOf(card)).containsExactly(resolves);
        assertThat(SagaWatch.ON_SAGA_FINAL_CHAPTER_ABILITY_TRIGGERS.effectsOf(card)).isEmpty();
    }

    @Test
    void aWatchHandsTheCollectorTheLimitAsTheKeyedWrapperWithAStableIdentity() {
        Card card = new Card();
        DrawCardEffect resolves = new DrawCardEffect(1);
        card.addTrigger(Triggers.finalChapterAbilityResolves(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), resolves);
        String key = card.getTriggeredAbilities().getFirst().limitKey();

        List<CardEffect> legacy = SagaWatch.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES.effectsOf(card);

        assertThat(legacy).containsExactly(OncePerTurnTriggerEffect.keyed(resolves, key));
        assertThat(SagaWatch.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES.registrationsOf(card))
                .extracting(EffectRegistration::effect).singleElement().isSameAs(legacy.getFirst());
    }
}
