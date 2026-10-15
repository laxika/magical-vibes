package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lifeblood.class, Mountain.class, Island.class})
class LifebloodTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's Mountain becoming tapped gains the controller 1 life")
    void opponentMountainTapGainsLife() {
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player2, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Each time an opponent's Mountain becomes tapped, the controller gains 1 life")
    void eachOpponentMountainTapTriggersSeparately() {
        harness.addToBattlefield(player1, new Lifeblood());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        mountain.untap();
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Tapping your own Mountain does not trigger")
    void ownMountainTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player1, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player1, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping an opponent's non-Mountain land does not trigger")
    void opponentNonMountainTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player2, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Two opponent Mountains each produce a life-gain trigger")
    void multipleMountainsTriggerIndependently() {
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 2);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("Each Lifeblood triggers for the same opponent Mountain")
    void multipleLifebloodsTriggerIndependently() {
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player2, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("A pending trigger still gains life after Lifeblood leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent lifeblood = harness.addToBattlefieldAndReturn(player1, new Lifeblood());
        harness.addToBattlefield(player2, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);
        assertThat(gd.stack.size() + gd.pendingManaAbilityTriggers.size()).isEqualTo(1);
        gd.playerBattlefields.get(player1.getId()).remove(lifeblood);
        gd.playerGraveyards.get(player1.getId()).add(lifeblood.getCard());
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
    }

}
