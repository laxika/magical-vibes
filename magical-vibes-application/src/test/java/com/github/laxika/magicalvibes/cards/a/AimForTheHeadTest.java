package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AimForTheHead.class, GrizzlyBears.class, WalkingCorpse.class})
class AimForTheHeadTest extends BaseCardTest {

    @Test
    @DisplayName("Exile mode exiles a target Zombie")
    void exileModeExilesTargetZombie() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        cast(0, zombie.getId());

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Hand mode makes the target opponent exile two cards")
    void handModeExilesTwoCardsFromTargetOpponentsHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        cast(1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exile mode cannot target a non-Zombie creature")
    void exileModeRequiresZombie() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Zombie");
    }

    @Test
    @DisplayName("Hand mode cannot target its controller")
    void handModeRequiresOpponent() {
        assertThatThrownBy(() -> cast(1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AimForTheHead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }
}
