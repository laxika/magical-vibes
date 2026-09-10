package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExertInfluence.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class ExertInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a creature whose power is within the one-color Converge value")
    void gainsControlWithOneColor() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castWithMana(ManaColor.BLUE, 5, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Gains control of a creature whose power equals the two-color Converge value")
    void gainsControlWithTwoColors() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Leaves a creature with greater power under its controller's control")
    void doesNotGainControlWhenPowerExceedsConvergeValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithMana(ManaColor.BLUE, 5, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castWithMana(ManaColor color, int amount, java.util.UUID targetId) {
        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, color, amount);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
