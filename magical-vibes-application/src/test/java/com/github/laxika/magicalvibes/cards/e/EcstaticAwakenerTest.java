package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EcstaticAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature, draws a card, and transforms")
    void sacrificesDrawsAndTransforms() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new EcstaticAwakener());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, awakener), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
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

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
