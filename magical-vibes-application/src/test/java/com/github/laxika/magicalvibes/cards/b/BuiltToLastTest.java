package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltToLastTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an artifact creature and grants it indestructible")
    void boostsArtifactCreatureAndGrantsIndestructible() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castResolve(ornithopter);

        assertThat(ornithopter.getPowerModifier()).isEqualTo(2);
        assertThat(ornithopter.getToughnessModifier()).isEqualTo(2);
        assertThat(ornithopter.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Boosts a nonartifact creature without granting indestructible")
    void boostsNonartifactCreatureWithoutIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bears);

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Boost and indestructible wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castResolve(ornithopter);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ornithopter.getPowerModifier()).isZero();
        assertThat(ornithopter.getToughnessModifier()).isZero();
        assertThat(ornithopter.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new BuiltToLast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
