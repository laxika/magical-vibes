package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Twigwalker.class, WoodlandDruid.class, Forest.class})
class TwigwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and gives two target creatures +2/+2")
    void sacrificesSelfAndBoostsTwoCreatures() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.assertNotOnBattlefield(player1, "Twigwalker");
        harness.assertInGraveyard(player1, "Twigwalker");

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondTarget)).isEqualTo(4);
    }

    @Test
    @DisplayName("The two-creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstTarget)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondTarget)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondTarget)).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void requiresExactlyTwoCreatureTargets() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects using the same creature for both targets")
    void rejectsDuplicateCreatureTarget() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires green mana in addition to the generic mana")
    void requiresGreenMana() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still boosts the remaining target if the other target leaves before resolution")
    void boostsRemainingTargetIfOtherLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new Twigwalker());
        Permanent removedTarget = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        Permanent remainingTarget = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(removedTarget.getId(), remainingTarget.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, removedTarget));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Woodland Druid");
        assertThat(gqs.getEffectivePower(gd, remainingTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, remainingTarget)).isEqualTo(4);
    }
}
