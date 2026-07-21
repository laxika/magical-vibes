package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonstrousCarabidTest extends BaseCardTest {

    // ===== Attacks each combat if able =====

    @Test
    @DisplayName("Declaring no attackers while Monstrous Carabid can attack throws exception")
    void mustAttackWhenAble() {
        Permanent carabid = new Permanent(new MonstrousCarabid());
        carabid.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(carabid);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Declaring Monstrous Carabid as attacker succeeds and deals 4 damage")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        Permanent carabid = new Permanent(new MonstrousCarabid());
        carabid.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(carabid);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Cycling {B/R} =====

    @Test
    @DisplayName("Cycling discards the card and draws one, paid with red")
    void cyclingDrawsACardWithRed() {
        harness.setHand(player1, List.of(new MonstrousCarabid()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Monstrous Carabid");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling can be paid with black")
    void cyclingDrawsACardWithBlack() {
        harness.setHand(player1, List.of(new MonstrousCarabid()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Monstrous Carabid");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
