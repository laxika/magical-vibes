package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirestormHellkite.class})
class FirestormHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Firestorm Hellkite")
    void paysCumulativeUpkeep() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(hellkite.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellkite);
    }

    @Test
    @DisplayName("Cumulative upkeep costs two blue and two red mana on the second upkeep")
    void paysIncreasingCumulativeUpkeep() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hellkite.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellkite);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Accepting without the required red mana sacrifices Firestorm Hellkite")
    void cannotPayWithoutBothColors() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hellkite);
        harness.assertInGraveyard(player1, "Firestorm Hellkite");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Firestorm Hellkite")
    void declineSacrifices() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hellkite);
        harness.assertInGraveyard(player1, "Firestorm Hellkite");
    }

    @Test
    @DisplayName("Age counters are added when the upkeep trigger resolves")
    void ageCounterWaitsForResolution() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(hellkite.getCounterCount(CounterType.AGE)).isZero();

        harness.passBothPriorities();

        assertThat(hellkite.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Firestorm Hellkite");
    }

    @Test
    @DisplayName("An opponent's upkeep does not trigger cumulative upkeep")
    void opponentUpkeepDoesNotTrigger() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(hellkite.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellkite);
    }

    @Test
    @DisplayName("An incomplete second upkeep payment sacrifices without spending mana")
    void cannotPayOnlyPartOfIncreasingUpkeep() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hellkite);
        harness.assertInGraveyard(player1, "Firestorm Hellkite");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
