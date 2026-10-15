package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RabidElephant.class, WoodlandDruid.class, AetherBurst.class})
class RabidElephantTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rabid Elephant gets +2/+2 until end of turn")
    void oneBlockerGivesPlusTwo() {
        Permanent elephant = addCreatureReady(player1, new RabidElephant());
        elephant.setAttacking(true);
        addCreatureReady(player2, new WoodlandDruid());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(elephant.getPowerModifier()).isEqualTo(2);
        assertThat(elephant.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("With two blockers Rabid Elephant gets +4/+4 until end of turn")
    void twoBlockersGivesPlusFour() {
        Permanent elephant = addCreatureReady(player1, new RabidElephant());
        elephant.setAttacking(true);
        addCreatureReady(player2, new WoodlandDruid());
        addCreatureReady(player2, new WoodlandDruid());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(elephant.getPowerModifier()).isEqualTo(4);
        assertThat(elephant.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent elephant = addCreatureReady(player1, new RabidElephant());
        elephant.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(elephant.getPowerModifier()).isZero();
        assertThat(elephant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent elephant = addCreatureReady(player1, new RabidElephant());
        elephant.setAttacking(true);
        addCreatureReady(player2, new WoodlandDruid());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(elephant.getPowerModifier()).isEqualTo(2);
        assertThat(elephant.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elephant.getPowerModifier()).isZero();
        assertThat(elephant.getToughnessModifier()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Only creatures still blocking at resolution contribute to the boost")
    void countsRemainingBlockersAtResolution(int blockerCount) {
        Permanent elephant = addCreatureReady(player1, new RabidElephant());
        elephant.setAttacking(true);
        Permanent departed = addCreatureReady(player2, new WoodlandDruid());
        if (blockerCount == 2) {
            addCreatureReady(player2, new WoodlandDruid());
        }
        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        prepareDeclareBlockers();
        List<BlockerAssignment> assignments = blockerCount == 1
                ? List.of(new BlockerAssignment(0, 0))
                : List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0));
        gs.declareBlockers(gd, player2, assignments);

        assertThat(gd.stack).hasSize(1);
        assertThat(elephant.getPowerModifier()).isZero();
        assertThat(elephant.getToughnessModifier()).isZero();

        harness.castInstant(player1, 0, List.of(departed.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player2, "Woodland Druid");
        resolveAllTriggers();

        assertThat(elephant.getPowerModifier()).isEqualTo(2 * (blockerCount - 1));
        assertThat(elephant.getToughnessModifier()).isEqualTo(2 * (blockerCount - 1));
    }
}
