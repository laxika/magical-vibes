package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

class AgonyWarpTest extends BaseCardTest {

    @Test
    @DisplayName("Applies -3/-0 to the first target and -0/-3 to the second target")
    void appliesBothBoostsToSeparateTargets() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        castAgonyWarp(List.of(first.getId(), second.getId()));

        assertThat(first.getPowerModifier()).isEqualTo(-3);
        assertThat(first.getToughnessModifier()).isEqualTo(0);
        assertThat(second.getPowerModifier()).isEqualTo(0);
        assertThat(second.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("The -0/-3 target dies to lethal toughness reduction")
    void toughnessReductionKillsSecondTarget() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        castAgonyWarp(List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(first.getId()))
                .noneMatch(p -> p.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Choosing the same creature twice stacks to -3/-3 and kills it")
    void sameTargetStacksToMinusThreeMinusThree() {
        Permanent creature = addCreature(player2);
        castAgonyWarp(List.of(creature.getId(), creature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        // first gets -3/-0 and survives; second gets -0/-3 and dies.
        castAgonyWarp(List.of(first.getId(), second.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(0);
        assertThat(first.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = addCreature(player2);
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new AgonyWarp()));
        addManaCost();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    private void castAgonyWarp(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new AgonyWarp()));
        addManaCost();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addManaCost() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
