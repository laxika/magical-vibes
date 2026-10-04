package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ImpatientIguana.class)
class ImpatientIguanaTest extends BaseCardTest {

    @Test
    void nonStartingPlayerMayBecomeStartingPlayerFromOpeningHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        ImpatientIguana iguana = new ImpatientIguana();
        openingHarness.setHand(openingHarness.getPlayer2(), List.of(iguana));
        openingHarness.skipMulligan();

        PendingInteraction.MayAbilityChoice choice = openingHarness.getGameData().interaction
                .activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(openingHarness.getPlayer2().getId());

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer2(), true);

        assertThat(openingHarness.getGameData().startingPlayerId)
                .isEqualTo(openingHarness.getPlayer2().getId());
        assertThat(openingHarness.getGameData().activePlayerId)
                .isEqualTo(openingHarness.getPlayer2().getId());
        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.RUNNING);
        assertThat(openingHarness.getGameData().playerHands
                .get(openingHarness.getPlayer2().getId())).containsExactly(iguana);
    }

    @Test
    void decliningOpeningHandChoiceLeavesStartingPlayerUnchanged() {
        GameTestHarness openingHarness = new GameTestHarness();
        ImpatientIguana iguana = new ImpatientIguana();
        openingHarness.setHand(openingHarness.getPlayer2(), List.of(iguana));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer2(), false);

        assertThat(openingHarness.getGameData().startingPlayerId)
                .isEqualTo(openingHarness.getPlayer1().getId());
        assertThat(openingHarness.getGameData().activePlayerId)
                .isEqualTo(openingHarness.getPlayer1().getId());
        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.RUNNING);
        assertThat(openingHarness.getGameData().playerHands
                .get(openingHarness.getPlayer2().getId())).containsExactly(iguana);
    }

    @Test
    void startingPlayerDoesNotReceiveTheOpeningHandChoice() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new ImpatientIguana()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isFalse();
        assertThat(openingHarness.getGameData().startingPlayerId)
                .isEqualTo(openingHarness.getPlayer1().getId());
        assertThat(openingHarness.getGameData().activePlayerId)
                .isEqualTo(openingHarness.getPlayer1().getId());
    }

    @Test
    void originalStartingPlayerKeepsFirstTurnWhenBothPlayersRevealIguana() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new ImpatientIguana()));
        openingHarness.setHand(openingHarness.getPlayer2(), List.of(new ImpatientIguana()));
        openingHarness.skipMulligan();

        boolean firstPlayerWasOfferedChoice = false;
        boolean secondPlayerWasOfferedChoice = false;
        for (int i = 0; i < 2; i++) {
            PendingInteraction.MayAbilityChoice choice = openingHarness.getGameData().interaction
                    .activeInteraction(PendingInteraction.MayAbilityChoice.class);
            if (choice == null) break;
            if (choice.playerId().equals(openingHarness.getPlayer1().getId())) {
                firstPlayerWasOfferedChoice = true;
                openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);
            } else {
                secondPlayerWasOfferedChoice = true;
                openingHarness.handleMayAbilityChosen(openingHarness.getPlayer2(), true);
            }
        }

        assertThat(firstPlayerWasOfferedChoice).isTrue();
        assertThat(secondPlayerWasOfferedChoice).isTrue();
        assertThat(openingHarness.getGameData().startingPlayerId)
                .isEqualTo(openingHarness.getPlayer1().getId());
        assertThat(openingHarness.getGameData().activePlayerId)
                .isEqualTo(openingHarness.getPlayer1().getId());
        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void canAttackOnTheTurnItIsCast() {
        harness.setHand(player1, List.of(new ImpatientIguana()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }
}
