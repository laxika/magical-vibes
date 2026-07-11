package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.c.Coercion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XiahouDunTheOneEyedTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns the target black card from the graveyard to hand")
    void returnsBlackCardFromGraveyard() {
        setupXiahouOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card black = new Coercion();
        harness.setGraveyard(player1, List.of(black));

        harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Coercion");
        harness.assertNotInGraveyard(player1, "Coercion");
        // Xiahou Dun was sacrificed as a cost.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Xiahou Dun, the One-Eyed"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Xiahou Dun, the One-Eyed"));
    }

    @Test
    @DisplayName("Cannot target a non-black card in the graveyard")
    void cannotTargetNonBlackCard() {
        setupXiahouOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card green = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(green));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, green.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupXiahouOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        Card black = new Coercion();
        harness.setGraveyard(player1, List.of(black));

        harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupXiahouOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Card black = new Coercion();
        harness.setGraveyard(player1, List.of(black));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new XiahouDunTheOneEyed());
        findPermanent(player1, "Xiahou Dun, the One-Eyed").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card black = new Coercion();
        harness.setGraveyard(player1, List.of(black));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupXiahouOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new XiahouDunTheOneEyed());
        findPermanent(player1, "Xiahou Dun, the One-Eyed").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
