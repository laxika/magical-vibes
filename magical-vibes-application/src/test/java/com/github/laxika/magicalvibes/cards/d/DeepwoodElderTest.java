package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepwoodElderTest extends BaseCardTest {

    @Test
    @DisplayName("X target lands become Forests and the activation discards a card")
    void changesXTargetLandsToForests() {
        Permanent elder = addCreatureReady(player1, new DeepwoodElder());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(island.getId(), forest.getId()));
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(island.getTransientLandTypeOverride()).isEqualTo(CardSubtype.FOREST);
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.FOREST);
        assertThat(elder.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Forest changes wear off at end of turn")
    void changesWearOffAtEndOfTurn() {
        Permanent island = activateOnTwoLands();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(island.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("The paid X limits the number of selected lands")
    void rejectsMoreTargetsThanX() {
        addCreatureReady(player1, new DeepwoodElder());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A non-land permanent is an illegal target")
    void rejectsNonLandTarget() {
        addCreatureReady(player1, new DeepwoodElder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent activateOnTwoLands() {
        addCreatureReady(player1, new DeepwoodElder());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(island.getId(), forest.getId()));
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        return island;
    }
}
