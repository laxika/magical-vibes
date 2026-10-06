package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DiregrafScavenger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvestigatorsJournal.class, DiregrafScavenger.class})
class InvestigatorsJournalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with suspect counters equal to the greatest creature count")
    void entersWithGreatestCreatureCountAmongPlayers() {
        harness.addToBattlefield(player1, new DiregrafScavenger());
        harness.addToBattlefield(player2, new DiregrafScavenger());
        harness.addToBattlefield(player2, new DiregrafScavenger());
        harness.addToBattlefield(player2, new DiregrafScavenger());
        harness.castFromHand(player1, new InvestigatorsJournal(), "{2}");
        harness.passBothPriorities();

        Permanent journal = findPermanent(player1, "Investigator's Journal");
        assertThat(journal.getCounterCount(CounterType.SUSPECT)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a suspect counter draws a card")
    void removesSuspectCounterAndDrawsCard() {
        Permanent journal = harness.addToBattlefieldAndReturn(player1, new InvestigatorsJournal());
        journal.setCounterCount(CounterType.SUSPECT, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(journal.getCounterCount(CounterType.SUSPECT)).isZero();
        assertThat(journal.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Sacrificing the Journal draws a card")
    void sacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new InvestigatorsJournal());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Investigator's Journal");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Enters without suspect counters when no player controls creatures")
    void entersWithZeroCountersWithoutCreatures() {
        harness.addToBattlefield(player2, new InvestigatorsJournal());
        harness.castFromHand(player1, new InvestigatorsJournal(), "{2}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Investigator's Journal")
                .getCounterCount(CounterType.SUSPECT)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts creatures at entry rather than when cast")
    void countsCreaturesWhenItEnters() {
        harness.addToBattlefield(player1, new DiregrafScavenger());
        harness.castFromHand(player1, new InvestigatorsJournal(), "{2}");
        harness.addToBattlefield(player1, new DiregrafScavenger());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Investigator's Journal")
                .getCounterCount(CounterType.SUSPECT)).isEqualTo(2);
    }

    @Test
    @DisplayName("Other counter types cannot pay the suspect counter cost")
    void cannotActivateWithoutSuspectCounter() {
        Permanent journal = harness.addToBattlefieldAndReturn(player1, new InvestigatorsJournal());
        journal.setCounterCount(CounterType.CHARGE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(journal.isTapped()).isFalse();
        assertThat(journal.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Journal cannot activate its counter-removal ability")
    void cannotActivateCounterAbilityWhileTapped() {
        Permanent journal = harness.addToBattlefieldAndReturn(player1, new InvestigatorsJournal());
        journal.setCounterCount(CounterType.SUSPECT, 1);
        journal.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(journal.getCounterCount(CounterType.SUSPECT)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both draws resolve after sacrificing the tapped Journal in response")
    void canSacrificeInResponseToCounterAbility() {
        Permanent journal = harness.addToBattlefieldAndReturn(player1, new InvestigatorsJournal());
        journal.setCounterCount(CounterType.SUSPECT, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(journal.isTapped()).isTrue();
        assertThat(journal.getCounterCount(CounterType.SUSPECT)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Investigator's Journal");
        harness.assertInGraveyard(player1, "Investigator's Journal");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }
}
