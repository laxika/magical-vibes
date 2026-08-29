package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WanderingArchaicTest extends BaseCardTest {

    @Test
    void frontFaceCastsAsCreature() {
        harness.setHand(player1, List.of(new WanderingArchaic()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wandering Archaic");
    }

    @Test
    void backFaceLetsEachPlayerTakeAlandAndGainLife() {
        harness.setHand(player1, List.of(new WanderingArchaic()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch)
                .as("stack=%s pending=%s", gd.stack, gd.pendingEffectResolutionEntry)
                .isNotNull();
        assertThat(firstSearch.params().playerId()).isEqualTo(player1.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().playerId()).isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    void opponentMayPayToPreventCopyingTheirInstant() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    void ifOpponentDoesNotPayControllerMayCopyTheInstant() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player1, 16);
    }
}
