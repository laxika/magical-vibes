package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gleancrawler.class, GrizzlyBears.class, Shock.class})
class GleancrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creature cards that were put into your graveyard from the battlefield this turn")
    void returnsCreaturesPutIntoGraveyardFromBattlefieldThisTurn() {
        Card alreadyInGraveyard = new GrizzlyBears();
        Card diedThisTurn = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));
        harness.addToBattlefield(player1, new Gleancrawler());
        harness.addToBattlefield(player1, diedThisTurn);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(diedThisTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(alreadyInGraveyard.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Card diedThisTurn = new GrizzlyBears();
        harness.addToBattlefield(player1, new Gleancrawler());
        harness.addToBattlefield(player1, diedThisTurn);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(diedThisTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(diedThisTurn.getId()));
    }

    @Test
    @DisplayName("Does not return creatures put into your graveyard during a previous turn")
    void doesNotReturnCreaturesFromPreviousTurn() {
        Card diedLastTurn = new GrizzlyBears();
        harness.addToBattlefield(player1, new Gleancrawler());
        harness.addToBattlefield(player1, diedLastTurn);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(diedLastTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(diedLastTurn.getId()));
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
