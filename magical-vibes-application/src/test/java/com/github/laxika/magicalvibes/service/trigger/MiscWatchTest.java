package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.PlanarTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MiscWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(MiscWatch.values(), ActionWatch.values(), SagaWatch.values(), LifeWatch.values(),
                        TapWatch.values(), LibraryWatch.values(), TargetWatch.values(), CounterWatch.values(),
                        DamageWatch.values(), CombatWatch.values(), SpellAbilityWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void everyMiscWatchReadsATriggerInAZoneItCanFunctionFrom() {
        assertThat(MiscWatch.values()).allSatisfy(watch -> {
            assertThat(watch.trigger()).as(watch.name()).isNotInstanceOf(PlanarTrigger.class);
            assertThat(watch.zone()).as(watch.name())
                    .isEqualTo(watch.name().startsWith("GRAVEYARD_") ? Zone.GRAVEYARD : Zone.BATTLEFIELD);
            assertThat(watch.trigger().defaultZones().functionsIn(watch.zone())
                    || watch.name().startsWith("GRAVEYARD_")).as(watch.name()).isTrue();
        });
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesOfItsTriggerAndZone() {
        Card card = new Card();
        DrawCardEffect unlocked = new DrawCardEffect(1);
        GainLifeEffect unlockedFromGraveyard = new GainLifeEffect(2);
        DrawCardEffect crewed = new DrawCardEffect(3);
        card.addTrigger(Triggers.youFullyUnlockRoom(), unlocked);
        card.addTrigger(new TriggerCondition(Triggers.youFullyUnlockRoom(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, unlockedFromGraveyard);
        card.addTrigger(Triggers.becomesCrewed(), crewed);

        assertThat(MiscAbilityReader.printedEffects(card, MiscWatch.ON_ALLY_ROOM_FULLY_UNLOCKED))
                .containsExactly(unlocked);
        assertThat(MiscAbilityReader.printedEffects(card, MiscWatch.GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED))
                .containsExactly(unlockedFromGraveyard);
        assertThat(MiscAbilityReader.printedEffects(card, MiscWatch.ON_SELF_BECOMES_CREWED)).containsExactly(crewed);
        assertThat(MiscAbilityReader.printedEffects(card, MiscWatch.ON_SELF_BECOMES_SADDLED)).isEmpty();
    }
}
