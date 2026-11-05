package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.TapTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TapWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(TapWatch.values(), LibraryWatch.values(), TargetWatch.values(), CounterWatch.values(),
                        DamageWatch.values(), CombatWatch.values(), SpellAbilityWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void everyTapWatchReadsATapTriggerOnTheBattlefield() {
        assertThat(TapWatch.values()).allSatisfy(watch -> {
            assertThat(watch.trigger()).as(watch.name()).isInstanceOf(TapTrigger.class);
            assertThat(watch.zone()).as(watch.name()).isEqualTo(Zone.BATTLEFIELD);
        });
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesOfItsTrigger() {
        Card card = new Card();
        DrawCardEffect untapped = new DrawCardEffect(1);
        GainLifeEffect allyUntapped = new GainLifeEffect(2);
        DrawCardEffect landTapped = new DrawCardEffect(3);
        card.addTrigger(Triggers.becomesUntapped(), untapped);
        card.addTrigger(Triggers.permanentYouControlBecomesUntapped(), allyUntapped);
        card.addTrigger(Triggers.playerTapsLandForMana(), landTapped);

        assertThat(TapAbilityReader.printedEffects(card, TapWatch.ON_SELF_BECOMES_UNTAPPED)).containsExactly(untapped);
        assertThat(TapAbilityReader.printedEffects(card, TapWatch.ON_ALLY_PERMANENT_BECOMES_UNTAPPED))
                .containsExactly(allyUntapped);
        assertThat(TapAbilityReader.printedEffects(card, TapWatch.ON_ANY_PLAYER_TAPS_LAND)).containsExactly(landTapped);
        assertThat(TapAbilityReader.printed(card, TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA)).isEmpty();
        assertThat(TapAbilityReader.printed(card, TapWatch.ON_ANY_PERMANENT_BECOMES_UNTAPPED)).isEmpty();
    }
}
