package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeOfHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature cannot attack this turn")
    void targetCreatureCannotAttackThisTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card changeOfHeart = new ChangeOfHeart();
        harness.setHand(player1, List.of(changeOfHeart));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(changeOfHeart);
    }

    @Test
    @DisplayName("The attack restriction expires at end of turn")
    void attackRestrictionExpiresAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card changeOfHeart = new ChangeOfHeart();
        harness.setHand(player1, List.of(changeOfHeart));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        advanceTurn();
        advanceTurn();

        assertThatCode(() -> declareAttackers(List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Buyback returns Change of Heart to its owner's hand as it resolves")
    void buybackReturnsSpellToHand() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card changeOfHeart = new ChangeOfHeart();
        harness.setHand(player1, List.of(changeOfHeart));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithBuyback(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(changeOfHeart);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Change of Heart cannot target a player")
    void cannotTargetPlayer() {
        Card changeOfHeart = new ChangeOfHeart();
        harness.setHand(player1, List.of(changeOfHeart));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
