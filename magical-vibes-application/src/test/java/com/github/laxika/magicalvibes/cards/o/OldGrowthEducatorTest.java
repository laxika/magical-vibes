package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OldGrowthEducatorTest extends BaseCardTest {

    

    @Test
    @DisplayName("Enters without counters if no life was gained this turn")
    void entersWithoutCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OldGrowthEducator()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (enters + ETB trigger)
        harness.passBothPriorities(); // resolve the ETB trigger (does nothing — no life gained)

        GameData gd = harness.getGameData();
        Permanent educator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Old-Growth Educator"))
                .findFirst().orElseThrow();
        assertThat(educator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters with two +1/+1 counters when you have gained life this turn")
    void entersWithCountersWhenLifeGained() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OldGrowthEducator()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (enters + ETB trigger)
        harness.passBothPriorities(); // resolve ETB effect (put counters)

        Permanent educator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Old-Growth Educator"))
                .findFirst().orElseThrow();
        assertThat(educator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(educator.getEffectivePower()).isEqualTo(6);
        assertThat(educator.getEffectiveToughness()).isEqualTo(6);
    }
}
