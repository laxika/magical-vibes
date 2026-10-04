package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EruditeWizard.class, GrizzlyBears.class})
class EruditeWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn puts a +1/+1 counter on Erudite Wizard")
    void secondDrawAddsCounterOnlyOnce() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new EruditeWizard());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        draw(player1.getId());
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger Erudite Wizard")
    void opponentDrawsDoNotAddCounters() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new EruditeWizard());
        gd.playerDecks.get(player2.getId()).add(new EruditeWizard());
        gd.playerDecks.get(player2.getId()).add(new EruditeWizard());

        draw(player2.getId());
        draw(player2.getId());
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's second draw triggers during an opponent's turn")
    void secondDrawOnOpponentsTurnAddsCounter() {
        gd.activePlayerId = player2.getId();
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new EruditeWizard());
        gd.playerDecks.get(player1.getId()).add(new EruditeWizard());
        gd.playerDecks.get(player1.getId()).add(new EruditeWizard());

        draw(player1.getId());
        draw(player1.getId());
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A draw before Erudite Wizard enters still counts as the first draw")
    void firstDrawBeforeEnteringStillCounts() {
        gd.playerDecks.get(player1.getId()).add(new EruditeWizard());
        gd.playerDecks.get(player1.getId()).add(new EruditeWizard());
        draw(player1.getId());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new EruditeWizard());

        draw(player1.getId());
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Erudite Wizard gets its own counter from the second draw")
    void multipleWizardsEachGetOneCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EruditeWizard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EruditeWizard());
        gd.playerDecks.get(player1.getId()).add(new EruditeWizard());
        gd.playerDecks.get(player1.getId()).add(new EruditeWizard());

        draw(player1.getId());
        draw(player1.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void draw(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

}
