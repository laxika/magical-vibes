package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DamageWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(DamageWatch.values(), CombatWatch.values(), SpellAbilityWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesFunctioningFromItsZone() {
        Card card = new Card();
        DrawCardEffect fromBattlefield = new DrawCardEffect(1);
        DrawCardEffect fromGraveyard = new DrawCardEffect(2);
        card.addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), fromBattlefield);
        card.addTrigger(new TriggerCondition(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT, fromGraveyard);

        assertThat(DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT.effectsOf(card))
                .containsExactly(fromBattlefield);
        assertThat(DamageWatch.GRAVEYARD_ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT.effectsOf(card))
                .containsExactly(fromGraveyard);
        assertThat(DamageWatch.GRAVEYARD_ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT.zone())
                .isEqualTo(Zone.GRAVEYARD);
        assertThat(DamageWatch.ON_DEALT_DAMAGE.effectsOf(card)).isEmpty();
    }
}
