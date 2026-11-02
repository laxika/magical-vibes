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

import static org.assertj.core.api.Assertions.assertThat;

class CombatWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Arrays.stream(CombatWatch.values()).map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesFunctioningFromItsZone() {
        Card card = new Card();
        DrawCardEffect fromBattlefield = new DrawCardEffect(1);
        DrawCardEffect fromGraveyard = new DrawCardEffect(2);
        card.addTrigger(Triggers.youAttack(), fromBattlefield);
        card.addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                fromGraveyard);

        assertThat(CombatWatch.ON_ALLY_CREATURES_ATTACK.effectsOf(card)).containsExactly(fromBattlefield);
        assertThat(CombatWatch.GRAVEYARD_ON_ALLY_CREATURES_ATTACK.effectsOf(card)).containsExactly(fromGraveyard);
        assertThat(CombatWatch.EXILE_ON_ALLY_CREATURES_ATTACK.effectsOf(card)).isEmpty();
        assertThat(CombatWatch.GRAVEYARD_ON_ALLY_CREATURES_ATTACK.zone()).isEqualTo(Zone.GRAVEYARD);
    }
}
