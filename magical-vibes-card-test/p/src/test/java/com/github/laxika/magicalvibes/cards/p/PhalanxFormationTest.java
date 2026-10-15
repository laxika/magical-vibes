package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArmoryOfIroas;
import com.github.laxika.magicalvibes.cards.r.RottedHulk;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhalanxFormation.class, RottedHulk.class, ArmoryOfIroas.class, Hubris.class})
class PhalanxFormationTest extends BaseCardTest {

    @Test
    @DisplayName("Each targeted creature gains double strike")
    void grantsDoubleStrikeToEachTarget() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new RottedHulk());
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(ownBear.getId(), opposingBear.getId()));

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void castsWithNoTargets() {
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Phalanx Formation");
    }

    @Test
    @DisplayName("Strive requires {1}{W} for each additional target")
    void chargesForEachAdditionalTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Only creature permanents can be targeted")
    void cannotTargetNonCreaturePermanent() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ArmoryOfIroas());
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Three targets cost seven mana including three white")
    void paysStriveForEveryTargetBeyondTheFirst() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new RottedHulk());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new RottedHulk());
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, third, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, untargeted, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The same creature cannot be chosen twice")
    void rejectsDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Any number of targets includes more than ninety-nine")
    void canTargetOneHundredCreatures() {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player1, new RottedHulk()));
        }
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.addMana(player1, ManaColor.WHITE, 100);
        harness.addMana(player1, ManaColor.COLORLESS, 101);

        harness.castAndResolveInstant(player1, 0, creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allSatisfy(creature ->
                assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue());
    }

    @Test
    @DisplayName("A remaining legal target gains double strike when another target leaves")
    void resolvesForRemainingLegalTarget() {
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        Permanent bounced = harness.addToBattlefieldAndReturn(player2, new RottedHulk());
        harness.setHand(player1, List.of(new PhalanxFormation()));
        harness.setHand(player2, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(remaining.getId(), bounced.getId()));
        harness.castAndResolveInstant(player2, 0, bounced.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Rotted Hulk");
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Phalanx Formation");
    }
}
