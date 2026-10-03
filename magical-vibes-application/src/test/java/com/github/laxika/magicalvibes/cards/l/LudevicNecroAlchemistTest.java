package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LudevicNecroAlchemist.class, GrizzlyBears.class, Shock.class})
class LudevicNecroAlchemistTest extends BaseCardTest {

    @Test
    @DisplayName("At each end step, the active player may draw if an opponent lost life")
    void activePlayerMayDrawAfterOpponentLostLife() {
        harness.addToBattlefield(player1, new LudevicNecroAlchemist());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        dealDamage(player2);

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the active player's draw leaves their library unchanged")
    void activePlayerMayDeclineToDraw() {
        harness.addToBattlefield(player1, new LudevicNecroAlchemist());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        dealDamage(player2);

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller's own life loss does not satisfy the condition")
    void controllerLifeLossDoesNotTrigger() {
        harness.addToBattlefield(player1, new LudevicNecroAlchemist());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        dealDamage(player1);

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private void dealDamage(Player target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
