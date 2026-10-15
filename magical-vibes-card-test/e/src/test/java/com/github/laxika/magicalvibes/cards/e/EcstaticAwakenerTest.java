package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EcstaticAwakener.class})
class EcstaticAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature, draws a card, and transforms")
    void sacrificesDrawsAndTransforms() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, awakener), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(awakener.isTransformed()).isTrue();
        assertThat(awakener.getCard().getName()).isEqualTo("Awoken Demon");
        assertThat(gqs.getEffectivePower(gd, awakener)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, awakener)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, awakener), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(awakener.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and activation cannot be repeated this turn")
    void cannotActivateAgainWhileFirstActivationIsOnStack() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        forceMainPhase(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, indexOf(player1, awakener), null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(awakener.isTransformed()).isFalse();
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, awakener), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remaining);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(awakener.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Can activate during an opponent's upkeep without tapping")
    void canActivateOnOpponentsTurnWhileTapped() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        harness.addToBattlefield(player1, new EcstaticAwakener());
        awakener.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, indexOf(player1, awakener), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(awakener.isTransformed()).isTrue();
        assertThat(awakener.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new EcstaticAwakener());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, awakener), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(awakener.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still draws when the source is sacrificed in response")
    void drawsEvenWhenSourceLeavesBattlefield() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        harness.addToBattlefield(player1, new EcstaticAwakener());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        forceMainPhase(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, indexOf(player1, awakener), null, null);
        Permanent secondAwakener = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        harness.activateAbility(player1, indexOf(player1, secondAwakener), null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(awakener.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(awakener);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(secondAwakener.isTransformed()).isTrue();
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
