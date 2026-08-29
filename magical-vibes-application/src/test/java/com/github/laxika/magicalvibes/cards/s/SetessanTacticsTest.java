package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
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

class SetessanTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature and grants it a tap-to-fight ability")
    void boostsAndGrantsFightAbility() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        castTactics(target.getId(), 1, 1);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, null, opponent.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(opponent.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Strive requires one additional green mana for each additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SetessanTactics()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstTarget.getId(), secondTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost and granted ability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTactics(target.getId(), 1, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SetessanTactics()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castTactics(java.util.UUID targetId, int greenMana, int colorlessMana) {
        harness.setHand(player1, List.of(new SetessanTactics()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
