package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalasResearcher.class, TalasScout.class})
class TalasResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Taps to draw a card during your turn before attackers")
    void tapsToDrawACard() {
        setupResearcherOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new TalasScout()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        assertThat(findPermanent(player1, "Talas Researcher").isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Can activate at the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupResearcherOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        harness.setLibrary(player1, List.of(new TalasScout()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate before attackers are declared in a later combat phase")
    void cannotActivateInLaterCombatPhase() {
        setupResearcherOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupResearcherOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new TalasResearcher());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent researcher = addCreatureReady(player1, new TalasResearcher());
        researcher.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"UPKEEP", "DRAW"})
    @DisplayName("Can draw during the beginning phase of your turn")
    void canActivateDuringBeginningPhase(TurnStep step) {
        setupResearcherOnMyTurn(step);
        TalasScout drawnCard = new TalasScout();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"DECLARE_BLOCKERS", "COMBAT_DAMAGE",
            "END_OF_COMBAT", "POSTCOMBAT_MAIN", "END_STEP"})
    @DisplayName("Cannot activate in later steps of your turn")
    void cannotActivateInLaterSteps(TurnStep step) {
        setupResearcherOnMyTurn(step);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
        assertThat(findPermanent(player1, "Talas Researcher").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new TalasResearcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(findPermanent(player1, "Talas Researcher").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draw ability resolves after its source leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        setupResearcherOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        TalasScout drawnCard = new TalasScout();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(gd.stack.getFirst().getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private void setupResearcherOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new TalasResearcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
