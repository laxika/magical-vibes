package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonstrousCarabid.class})
class MonstrousCarabidTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring no attackers while Monstrous Carabid can attack throws exception")
    void mustAttackWhenAble() {
        Permanent carabid = harness.addToBattlefieldAndReturn(player1, new MonstrousCarabid());
        carabid.setSummoningSick(false);

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

        Permanent carabid = harness.addToBattlefieldAndReturn(player1, new MonstrousCarabid());
        carabid.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one, paid with red")
    void cyclingDrawsACardWithRed() {
        harness.setHand(player1, List.of(new MonstrousCarabid()));
        harness.setLibrary(player1, List.of(new MonstrousCarabid()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Monstrous Carabid");
        harness.assertInHand(player1, "Monstrous Carabid");
    }

    @Test
    @DisplayName("Cycling can be paid with black")
    void cyclingDrawsACardWithBlack() {
        harness.setHand(player1, List.of(new MonstrousCarabid()));
        harness.setLibrary(player1, List.of(new MonstrousCarabid()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Monstrous Carabid");
        harness.assertInHand(player1, "Monstrous Carabid");
    }
    @Test
    void summoningSickCarabidDoesNotHaveToAttack() {
        Permanent carabid = harness.addToBattlefieldAndReturn(player1, new MonstrousCarabid());
        carabid.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(carabid.isAttacking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void tappedCarabidDoesNotHaveToAttack() {
        Permanent carabid = harness.addToBattlefieldAndReturn(player1, new MonstrousCarabid());
        carabid.setSummoningSick(false);
        carabid.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(carabid.isAttacking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void cyclingDiscardsImmediatelyButDrawsOnlyOnResolution() {
        MonstrousCarabid cycledCard = new MonstrousCarabid();
        MonstrousCarabid drawnCard = new MonstrousCarabid();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingCannotBePaidWithColorlessMana() {
        MonstrousCarabid card = new MonstrousCarabid();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
