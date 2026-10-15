package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialFormation.class, GrizzlyBears.class, Forest.class})
class AerialFormationTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts each target creature and grants flying")
    void boostsAndGrantsFlyingToEachTarget() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(ownBear.getId(), opposingBear.getId()));

        for (Permanent bear : List.of(ownBear, opposingBear)) {
            assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Strive requires {2}{U} for each additional target")
    void chargesForEachAdditionalTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can be cast with zero targets for only its base cost")
    void canBeCastWithZeroTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Aerial Formation");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseTheSameCreatureTwice() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still affects a remaining target when another leaves the battlefield")
    void resolvesForRemainingTarget() {
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(remaining.getId(), removed.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remaining)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, remaining)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Aerial Formation");
    }

    @Test
    @DisplayName("Any number of targets includes one hundred creatures")
    void canTargetOneHundredCreatures() {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        }
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 100);
        harness.addMana(player1, ManaColor.COLORLESS, 198);

        harness.castAndResolveInstant(player1, 0, creatures.stream().map(Permanent::getId).toList());

        for (Permanent creature : creatures) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        }
    }


    @Test
    @DisplayName("Three targets require two Strive payments")
    void threeTargetsRequireTwoStrivePayments() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialFormation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        List<UUID> targets = List.of(first.getId(), second.getId(), third.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Aerial Formation");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, targets);

        for (Permanent creature : List.of(first, second, third)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        }
    }

}
