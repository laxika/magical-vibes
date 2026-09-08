package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvestigatorsJournal.class, GrizzlyBears.class})
class InvestigatorsJournalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with suspect counters equal to the greatest creature count")
    void entersWithGreatestCreatureCountAmongPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvestigatorsJournal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
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
        Permanent journal = harness.addToBattlefieldAndReturn(player1, new InvestigatorsJournal());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(journal.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
