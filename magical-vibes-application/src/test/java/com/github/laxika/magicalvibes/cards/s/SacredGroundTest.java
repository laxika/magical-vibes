package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SacredGroundTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput() && guard++ < 20) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Opponent's spell destroying your land returns it to the battlefield")
    void opponentDestroysYourLandReturnsIt() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.castSorcery(player2, 0, mountainId);
        resolveStack();

        GameData gd = harness.getGameData();
        // Sacred Ground returned the land to its owner's battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Mountain"));
    }

    @Test
    @DisplayName("Your own spell destroying your own land does not trigger Sacred Ground")
    void ownSpellDestroyingOwnLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.castSorcery(player1, 0, mountainId);
        resolveStack();

        GameData gd = harness.getGameData();
        // Cause is controlled by the land's owner, so the land stays in the graveyard.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"));
    }

    @Test
    @DisplayName("Opponent destroying their own land does not trigger your Sacred Ground")
    void opponentDestroyingTheirOwnLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SacredGround());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player2, 0, mountainId);
        resolveStack();

        GameData gd = harness.getGameData();
        // The land went to the opponent's graveyard, not the Sacred Ground controller's, so no trigger.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"));
    }
}
