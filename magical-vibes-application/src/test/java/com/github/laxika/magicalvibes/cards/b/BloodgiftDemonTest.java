package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.VictimOfNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodgiftDemon.class, VictimOfNight.class})
class BloodgiftDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Controller targets self: draws a card and loses 1 life")
    void targetSelfDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        // Choose player1 as target
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Controller targets opponent: opponent draws a card and loses 1 life")
    void targetOpponentDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        // Choose player2 as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller's life is unchanged when targeting opponent")
    void controllersLifeUnchangedWhenTargetingOpponent() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Demon controlled by the second player triggers on that player's upkeep")
    void triggersOnSecondPlayersUpkeep() {
        harness.addToBattlefield(player2, new BloodgiftDemon());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Upkeep trigger still draws and loses life after the Demon is destroyed")
    void triggerResolvesAfterSourceIsDestroyed() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Bloodgift Demon"));

        harness.assertInGraveyard(player1, "Bloodgift Demon");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 19);
    }
}
