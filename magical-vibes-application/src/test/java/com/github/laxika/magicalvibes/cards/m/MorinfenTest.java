package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Morinfen.class)
class MorinfenTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep costs 1 life per age counter")
    void paysCumulativeUpkeepInLife() {
        Permanent morinfen = harness.addToBattlefieldAndReturn(player1, new Morinfen());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(morinfen.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(morinfen);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Third upkeep costs 3 life")
    void thirdUpkeepCostsThreeLife() {
        Permanent morinfen = harness.addToBattlefieldAndReturn(player1, new Morinfen());
        morinfen.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(morinfen.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(morinfen);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during Morinfen's controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        Permanent morinfen = harness.addToBattlefieldAndReturn(player1, new Morinfen());

        advanceToUpkeep(player2);

        assertThat(morinfen.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(morinfen);
    }

    @Test
    @DisplayName("Morinfen is sacrificed when its cumulative upkeep cannot be paid")
    void cannotPayUpkeepWhenLifeIsInsufficient() {
        Permanent morinfen = harness.addToBattlefieldAndReturn(player1, new Morinfen());
        morinfen.setCounterCount(CounterType.AGE, 1);
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(morinfen);
        harness.assertInGraveyard(player1, "Morinfen");
        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Morinfen")
    void decliningUpkeepSacrifices() {
        Permanent morinfen = harness.addToBattlefieldAndReturn(player1, new Morinfen());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(morinfen);
        harness.assertInGraveyard(player1, "Morinfen");
    }
}
