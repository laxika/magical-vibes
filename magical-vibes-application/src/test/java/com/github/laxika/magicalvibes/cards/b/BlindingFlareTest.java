package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

class BlindingFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Makes each target creature unable to block this turn")
    void makesEachTargetCreatureUnableToBlock() {
        Permanent firstCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(firstCreature.isCantBlockThisTurn()).isTrue();
        assertThat(secondCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Strive requires one additional red mana per additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void canBeCastWithNoTargets() {
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Can't-block effect wears off at end of turn")
    void cantBlockWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isCantBlockThisTurn()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
