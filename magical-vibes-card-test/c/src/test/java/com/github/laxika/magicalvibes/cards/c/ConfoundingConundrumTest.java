package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VastwoodSurge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConfoundingConundrum.class, Forest.class, Mountain.class, VastwoodSurge.class})
class ConfoundingConundrumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card")
    void entersAndDrawsCard() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new ConfoundingConundrum());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("First opponent land does not trigger")
    void firstOpponentLandDoesNotTrigger() {
        addConundrum();

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second opponent land triggers that player to return a land")
    void secondOpponentLandReturnsOpponentsLand() {
        addConundrum();
        Permanent controllerLand = harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        Permanent returnedLand = harness.enterBattlefieldAndReturn(player2, new Mountain());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, returnedLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(controllerLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getCard()).isInstanceOf(Forest.class);
        assertThat(gd.playerHands.get(player2.getId())).contains(returnedLand.getCard());
    }

    @Test
    void controllersLandEntriesDoNotTrigger() {
        addConundrum();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Mountain());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void opponentCanReturnLandThatDidNotEnterThisTurn() {
        addConundrum();
        Permanent oldLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent firstLand = harness.enterBattlefieldAndReturn(player2, new Forest());
        Permanent secondLand = harness.enterBattlefieldAndReturn(player2, new Forest());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, oldLand.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerHands.get(player2.getId())).contains(oldLand.getCard());
    }

    @Test
    void simultaneousFirstTwoLandsEachTrigger() {
        harness.addToBattlefield(player2, new ConfoundingConundrum());
        harness.setHand(player1, List.of(new VastwoodSurge()));
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(forest, mountain));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(2);
        Permanent firstLand = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstLand.getId());
        Permanent remainingLand = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, remainingLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(forest, mountain);
    }

    private void addConundrum() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new ConfoundingConundrum());
        harness.passBothPriorities();
    }
}
