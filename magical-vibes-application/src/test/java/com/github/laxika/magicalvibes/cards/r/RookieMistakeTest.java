package com.github.laxika.magicalvibes.cards.r;

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

class RookieMistakeTest extends BaseCardTest {

    @Test
    @DisplayName("Applies +0/+2 to the first target and -2/-0 to the second target")
    void appliesBothBoostsToSeparateTargets() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        castRookieMistake(List.of(first.getId(), second.getId()));

        assertThat(first.getPowerModifier()).isEqualTo(0);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(-2);
        assertThat(second.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast with duplicate targets")
    void cannotCastWithDuplicateTargets() {
        Permanent creature = addCreature(player2);
        harness.setHand(player1, List.of(new RookieMistake()));
        addManaCost();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = addCreature(player2);
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new RookieMistake()));
        addManaCost();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    @Test
    @DisplayName("Boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        castRookieMistake(List.of(first.getId(), second.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }

    private void castRookieMistake(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new RookieMistake()));
        addManaCost();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addManaCost() {
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
