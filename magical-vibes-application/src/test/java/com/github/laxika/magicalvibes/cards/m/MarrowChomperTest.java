package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarrowChomper.class, QasaliPridemage.class, Terminate.class})
class MarrowChomperTest extends BaseCardTest {

    private void castMarrowChomper() {
        harness.castFromHand(player1, new MarrowChomper(), "{3}{B}{G}");
    }

    private Permanent marrowChomper() {
        return findPermanent(player1, "Marrow Chomper");
    }

    @Test
    @DisplayName("Devouring two creatures adds four +1/+1 counters and gains 4 life")
    void devourTwoAddsFourCountersAndGainsFourLife() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castMarrowChomper();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        // Fodder is gone; Marrow Chomper enters with 4 +1/+1 counters (devour 2 x 2 creatures).
        Permanent marrowChomper = marrowChomper();
        assertThat(marrowChomper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities(); // resolve life-gain trigger

        // 2 life per devoured creature = 4 life.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters and gains no life")
    void devourNoneNoCountersNoLife() {
        harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castMarrowChomper();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent marrowChomper = marrowChomper();
        assertThat(marrowChomper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities(); // resolve life-gain trigger (gains 0)

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("With no creatures to devour, the life-gain trigger still resolves for zero")
    void noCreaturesToDevour() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castMarrowChomper();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(marrowChomper().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Devour can sacrifice only a subset of your creatures and never an opponent's")
    void devourOnlyOneOfTwoCreatures() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new QasaliPridemage());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castMarrowChomper();
        harness.passBothPriorities();

        var choice = (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosen.getId(), kept.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(marrowChomper().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kept).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Qasali Pridemage");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Removing Marrow Chomper in response does not erase its devoured count")
    void gainsLifeAfterSourceIsDestroyed() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castMarrowChomper();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, marrowChomper().getId());

        harness.assertNotOnBattlefield(player1, "Marrow Chomper");
        harness.assertInGraveyard(player1, "Marrow Chomper");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
