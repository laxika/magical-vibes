package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({BountyOfMight.class, GrizzlyBears.class, FountainOfYouth.class, Unsummon.class})
class BountyOfMightTest extends BaseCardTest {

    @Test
    @DisplayName("Gives each of three target creatures +3/+3")
    void boostsThreeTargetCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());

        castBountyOfMight(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(5);
    }

    @Test
    @DisplayName("Allows the same creature to receive all three boosts")
    void stacksAllThreeBoostsOnOneTarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castBountyOfMight(List.of(creature.getId(), creature.getId(), creature.getId()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);
    }

    @Test
    @DisplayName("Boosts wear off at cleanup")
    void boostsWearOffAtCleanup() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castBountyOfMight(List.of(creature.getId(), creature.getId(), creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BountyOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can split boosts between your creature and an opponent's creature")
    void splitsBoostsBetweenTwoCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        castBountyOfMight(List.of(first.getId(), second.getId(), first.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    @DisplayName("Remaining target gets only its own boost when repeated targets leave")
    void resolvesWithOnlyLastTargetLegal() {
        Permanent removed = addCreatureReady(player1, new GrizzlyBears());
        Permanent remaining = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BountyOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castInstant(player1, 0, List.of(removed.getId(), removed.getId(), remaining.getId()));

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, removed.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, remaining)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, remaining)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Bounty of Might");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not resolve when its only targeted creature leaves")
    void doesNotResolveWhenAllTargetsAreIllegal() {
        Permanent removed = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BountyOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castInstant(player1, 0, List.of(removed.getId(), removed.getId(), removed.getId()));

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, removed.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Bounty of Might");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires a target for each of the three boosts")
    void cannotOmitATarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BountyOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void castBountyOfMight(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BountyOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
