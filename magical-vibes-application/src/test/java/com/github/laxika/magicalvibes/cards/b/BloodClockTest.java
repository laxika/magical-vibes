package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodClock.class, SakuraTribeScout.class})
class BloodClockTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may pay 2 life to keep their permanents")
    void activePlayerMayPayLife() {
        harness.addToBattlefield(player1, new BloodClock());
        harness.addToBattlefield(player2, new SakuraTribeScout());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("The controller may pay 2 life during their own upkeep")
    void controllerMayPayLifeDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new BloodClock());
        harness.addToBattlefield(player1, new SakuraTribeScout());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("If the active player declines, they choose a permanent they control to return")
    void decliningPaymentReturnsChosenPermanent() {
        harness.addToBattlefield(player1, new BloodClock());
        Permanent firstScout = harness.addToBattlefieldAndReturn(player2, new SakuraTribeScout());
        harness.addToBattlefield(player2, new SakuraTribeScout());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(firstScout.getId());
        harness.handlePermanentChosen(player2, firstScout.getId());

        harness.assertInHand(player2, "Sakura-Tribe Scout");
        assertThat(countPermanents(player2, "Sakura-Tribe Scout")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Blood Clock");
    }

    @Test
    @DisplayName("A player who cannot pay 2 life still chooses a permanent to return")
    void cannotPayLifeReturnsPermanent() {
        harness.addToBattlefield(player1, new BloodClock());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new SakuraTribeScout());
        harness.setLife(player2, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, scout.getId());

        harness.assertLife(player2, 1);
        harness.assertInHand(player2, "Sakura-Tribe Scout");
        harness.assertNotOnBattlefield(player2, "Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("The returned permanent goes to its owner's hand")
    void returnsPermanentToItsOwnersHand() {
        harness.addToBattlefield(player1, new BloodClock());
        Card scoutCard = new SakuraTribeScout();
        scoutCard.setOwnerId(player1.getId());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, scoutCard);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player2, scout.getId());

        harness.assertInHand(player1, "Sakura-Tribe Scout");
        harness.assertNotInHand(player2, "Sakura-Tribe Scout");
        harness.assertNotOnBattlefield(player2, "Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("Declining with no permanent to return does nothing")
    void decliningWithNoPermanentDoesNothing() {
        harness.addToBattlefield(player1, new BloodClock());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Blood Clock");
    }
}
