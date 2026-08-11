package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

class GrowthCycleTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+3 with no Growth Cycle in the graveyard")
    void givesBaseBoostWithEmptyGraveyard() {
        Permanent target = addCreature(player2);
        castGrowthCycle(target);

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Gives an additional +2/+2 for each Growth Cycle in the controller's graveyard")
    void boostScalesWithNamedCardsInGraveyard() {
        Permanent target = addCreature(player2);
        harness.setGraveyard(player1, List.of(new GrowthCycle(), new GrowthCycle(), new FountainOfYouth()));
        castGrowthCycle(target);

        assertThat(target.getPowerModifier()).isEqualTo(7);
        assertThat(target.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("Counts only the controller's graveyard")
    void ignoresOpponentGraveyard() {
        Permanent target = addCreature(player2);
        harness.setGraveyard(player2, List.of(new GrowthCycle(), new GrowthCycle()));
        castGrowthCycle(target);

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent target = addCreature(player2);
        castGrowthCycle(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new GrowthCycle()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void castGrowthCycle(Permanent target) {
        harness.setHand(player1, List.of(new GrowthCycle()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
