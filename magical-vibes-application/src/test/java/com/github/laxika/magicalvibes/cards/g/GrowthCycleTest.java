package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.ManifoldKey;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrowthCycle.class, GreenwoodSentinel.class, ManifoldKey.class})
class GrowthCycleTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+3 with no Growth Cycle in the graveyard")
    void givesBaseBoostWithEmptyGraveyard() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        castGrowthCycle(target);

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Gives an additional +2/+2 for each Growth Cycle in the controller's graveyard")
    void boostScalesWithNamedCardsInGraveyard() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setGraveyard(player1, List.of(new GrowthCycle(), new GrowthCycle(), new ManifoldKey()));
        castGrowthCycle(target);

        assertThat(target.getPowerModifier()).isEqualTo(7);
        assertThat(target.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("Counts only the controller's graveyard")
    void ignoresOpponentGraveyard() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setGraveyard(player2, List.of(new GrowthCycle(), new GrowthCycle()));
        castGrowthCycle(target);

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManifoldKey());
        harness.setHand(player1, List.of(new GrowthCycle()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts graveyard cards at resolution and fixes the bonus afterward")
    void countsAtResolutionAndDoesNotRecalculateAfterward() {
        Permanent target = addCreatureReady(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new GrowthCycle()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        harness.setGraveyard(player1, List.of(new GrowthCycle()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.setGraveyard(player1, List.of());
        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not resolve when its only target has left the battlefield")
    void doesNotBoostAnotherCreatureWhenTargetLeaves() {
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent other = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new GrowthCycle()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Growth Cycle");
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
