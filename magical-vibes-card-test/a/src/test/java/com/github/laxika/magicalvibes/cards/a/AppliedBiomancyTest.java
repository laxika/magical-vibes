package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
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

@CardUsed({AppliedBiomancy.class, SauroformHybrid.class, SimicGuildgate.class})
class AppliedBiomancyTest extends BaseCardTest {

    @Test
    @DisplayName("Boost mode gives target creature +1/+1 until end of turn")
    void boostModeGivesPlusOnePlusOne() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bounce mode returns target creature to its owner's hand")
    void bounceModeReturnsCreatureToHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        cast(new int[]{1}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertInHand(player2, "Sauroform Hybrid");
    }

    @Test
    @DisplayName("Both modes can target the same creature")
    void bothModesShareTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));

        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertInHand(player2, "Sauroform Hybrid");
    }

    @Test
    @DisplayName("Modes cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new SauroformHybrid());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SimicGuildgate());
        harness.setHand(player1, List.of(new AppliedBiomancy()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes affect only their respective targets")
    void bothModesHaveSeparateTargets() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent bounced = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        cast(new int[]{0, 1}, List.of(boosted.getId(), bounced.getId()));

        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertInHand(player2, "Sauroform Hybrid");
    }

    @Test
    @DisplayName("The boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        cast(new int[]{0}, List.of(creature.getId()));
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bounce still resolves when the boost target leaves the battlefield")
    void bounceResolvesWhenBoostTargetLeaves() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent bounced = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.setHand(player1, List.of(new AppliedBiomancy()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(boosted.getId(), bounced.getId()));

        harness.setHand(player2, List.of(new AppliedBiomancy()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castModalInstantWithModes(player2, 0, 1, 2,
                new int[]{1}, List.of(boosted.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertInHand(player1, "Sauroform Hybrid");
        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertInHand(player2, "Sauroform Hybrid");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost still resolves when the bounce target leaves the battlefield")
    void boostResolvesWhenBounceTargetLeaves() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent bounced = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.setHand(player1, List.of(new AppliedBiomancy()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(boosted.getId(), bounced.getId()));

        harness.setHand(player2, List.of(new AppliedBiomancy()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castModalInstantWithModes(player2, 0, 1, 2,
                new int[]{1}, List.of(bounced.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertInHand(player2, "Sauroform Hybrid");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen creature returns to its owner's hand")
    void stolenCreatureReturnsToOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        cast(new int[]{1}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertInHand(player2, "Sauroform Hybrid");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new AppliedBiomancy()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
