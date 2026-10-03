package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecorumDissertation.class})
class DecorumDissertationTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards and loses 2 life")
    void targetPlayerDrawsTwoCardsAndLoses2Life() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castDecorumDissertationTargeting(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller may target themselves and only the target loses life")
    void controllerMayTargetThemselves() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castDecorumDissertationTargeting(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Paradigm exiles the original and casts a free copy with a new target")
    void paradigmCopyCanChooseDifferentTargetAndCeasesToExist() {
        castDecorumDissertationTargeting(player2.getId());
        assertThat(gd.exiledCards).anyMatch(e -> e.card() instanceof DecorumDissertation);
        harness.assertNotInGraveyard(player1, "Decorum Dissertation");
        harness.setHand(player1, List.of());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card() instanceof DecorumDissertation)).hasSize(1);
        harness.assertNotInGraveyard(player1, "Decorum Dissertation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining a paradigm copy does not prevent casting one in a later main phase")
    void decliningCopyPreservesFutureTriggers() {
        castDecorumDissertationTargeting(player2.getId());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card() instanceof DecorumDissertation)).hasSize(1);

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Resolving a second original creates only one recurring paradigm trigger")
    void secondOriginalDoesNotDuplicateParadigmTrigger() {
        castDecorumDissertationTargeting(player2.getId());
        castDecorumDissertationTargeting(player2.getId());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card() instanceof DecorumDissertation)).hasSize(2);
    }

    private void advanceToFirstMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }

    private void castDecorumDissertationTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DecorumDissertation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
