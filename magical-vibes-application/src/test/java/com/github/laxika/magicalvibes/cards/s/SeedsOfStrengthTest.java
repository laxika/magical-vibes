package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({SeedsOfStrength.class, GrizzlyBears.class, Mountain.class})
class SeedsOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives three target creatures +1/+1 each")
    void boostsThreeTargetCreatures() {
        Permanent first = addCreature();
        Permanent second = addCreature();
        Permanent third = addCreature();

        castSeedsOfStrength(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows the same creature to be targeted three times")
    void stacksAllThreeBoostsOnOneTarget() {
        Permanent creature = addCreature();

        castSeedsOfStrength(List.of(creature.getId(), creature.getId(), creature.getId()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Requires exactly three creature targets")
    void requiresExactlyThreeTargets() {
        Permanent creature = addCreature();
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent first = addCreature();
        Permanent second = addCreature();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(first.getId(), second.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The boosts expire at end of turn")
    void boostsExpireAtEndOfTurn() {
        Permanent creature = addCreature();
        castSeedsOfStrength(List.of(creature.getId(), creature.getId(), creature.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
    }

    private void castSeedsOfStrength(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
