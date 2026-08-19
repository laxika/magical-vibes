package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CherishedHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("After it dies, Dinosaur creature spells can be cast during the opponent's turn")
    void grantsFlashToDinosaursThisTurn() {
        destroyHatchlingAndResolveDeathTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RaptorHatchling()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Raptor Hatchling"));
    }

    @Test
    @DisplayName("The death ability does not grant flash to non-Dinosaur creature spells")
    void doesNotGrantFlashToNonDinosaurs() {
        destroyHatchlingAndResolveDeathTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A Dinosaur cast this turn gains the optional enter-the-battlefield fight ability")
    void DinosaurGainsFightAbility() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        destroyHatchlingAndResolveDeathTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RaptorHatchling()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertNotOnBattlefield(player1, "Raptor Hatchling");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void destroyHatchlingAndResolveDeathTriggers() {
        Permanent hatchling = addCreatureReady(player1, new CherishedHatchling());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hatchling.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cherished Hatchling");
    }
}
