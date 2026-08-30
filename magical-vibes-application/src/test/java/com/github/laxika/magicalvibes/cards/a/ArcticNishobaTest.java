package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArcticNishobaTest extends BaseCardTest {

    @Test
    @DisplayName("Cumulative upkeep can be paid with white mana")
    void cumulativeUpkeepCanBePaidWithWhiteMana() {
        Permanent nishoba = harness.addToBattlefieldAndReturn(player1, new ArcticNishoba());
        nishoba.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(nishoba.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nishoba);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices it and gains twice its age counters in life")
    void decliningCumulativeUpkeepSacrificesItAndGainsLife() {
        Permanent nishoba = harness.addToBattlefieldAndReturn(player1, new ArcticNishoba());
        nishoba.setCounterCount(CounterType.AGE, 2);
        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(nishoba.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nishoba);
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Death trigger gains twice the age counters")
    void deathTriggerGainsTwiceTheAgeCounters() {
        Permanent nishoba = harness.addToBattlefieldAndReturn(player1, new ArcticNishoba());
        nishoba.setCounterCount(CounterType.AGE, 3);
        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nishoba);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nishoba.getCard());
        harness.assertLife(player1, 26);
    }
}
