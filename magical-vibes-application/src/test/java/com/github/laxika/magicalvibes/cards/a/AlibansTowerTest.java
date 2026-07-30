package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlibansTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature +3/+1 until end of turn")
    void boostsBlockingCreature() {
        Permanent blocker = addBlockingBear(player1);
        setupTower();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Aliban's Tower");
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent blocker = addBlockingBear(player1);
        setupTower();

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addBlockingBear(player1);
        Permanent bystander = new Permanent(new GrizzlyBears());
        bystander.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bystander);
        setupTower();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    private void setupTower() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AlibansTower()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addBlockingBear(Player player) {
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        bear.setBlocking(true);
        gd.playerBattlefields.get(player.getId()).add(bear);
        return bear;
    }
}
