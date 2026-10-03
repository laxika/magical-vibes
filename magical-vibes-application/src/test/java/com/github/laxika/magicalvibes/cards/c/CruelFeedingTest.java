package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelFeeding.class, GrizzlyBears.class, Forest.class, GoldenHind.class})
class CruelFeedingTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts each target creature and grants lifelink")
    void boostsAndGrantsLifelinkToEachTarget() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, List.of(ownBear.getId(), opposingBear.getId()));
        harness.passBothPriorities();

        for (Permanent bear : List.of(ownBear, opposingBear)) {
            assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    @DisplayName("Strive requires {2}{B} for each additional target")
    void chargesForEachAdditionalTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can be cast with no targets for just one black mana")
    void canBeCastWithNoTargets() {
        Permanent hind = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, List.<UUID>of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, hind)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hind, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Three targets cost four generic and three black mana")
    void paysStriveForEveryTargetBeyondTheFirst() {
        List<Permanent> hinds = IntStream.range(0, 3)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GoldenHind()))
                .toList();
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, hinds.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        for (Permanent hind : hinds) {
            assertThat(gqs.getEffectivePower(gd, hind)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, hind)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, hind, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    @DisplayName("A creature cannot be chosen twice for the same target group")
    void rejectsDuplicateTargets() {
        Permanent hind = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(hind.getId(), hind.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still affects the remaining target when another leaves the battlefield")
    void resolvesForRemainingLegalTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(removed);
        gd.playerGraveyards.get(player1.getId()).add(removed.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remaining)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.LIFELINK)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing creature's lifelink gains life for its controller")
    void lifelinkGainsLifeForCreatureController() {
        Permanent hind = addCreatureReady(player2, new GoldenHind());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, hind.getId());
        harness.passBothPriorities();
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Any number of targets includes more than ninety-nine creatures")
    void canTargetOneHundredCreatures() {
        List<Permanent> hinds = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GoldenHind()))
                .toList();
        harness.setHand(player1, List.of(new CruelFeeding()));
        harness.addMana(player1, ManaColor.BLACK, 100);
        harness.addMana(player1, ManaColor.COLORLESS, 198);

        harness.castInstant(player1, 0, hinds.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        for (Permanent hind : hinds) {
            assertThat(gqs.getEffectivePower(gd, hind)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, hind, Keyword.LIFELINK)).isTrue();
        }
    }
}
