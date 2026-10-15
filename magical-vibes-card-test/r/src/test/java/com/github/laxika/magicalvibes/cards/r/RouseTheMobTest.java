package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({RouseTheMob.class, GrizzlyBears.class, Forest.class, Shock.class})
class RouseTheMobTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts each target creature and grants trample")
    void boostsAndGrantsTrampleToEachTarget() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(ownBear.getId(), opposingBear.getId()));
        harness.passBothPriorities();

        for (Permanent bear : List.of(ownBear, opposingBear)) {
            assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Strive requires {2}{R} for each additional target")
    void chargesForEachAdditionalTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can be cast with zero targets for just its base cost")
    void canBeCastWithoutTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.<UUID>of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rouse the Mob");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The same creature cannot be chosen twice")
    void rejectsDuplicateTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Remaining legal targets receive both effects when another target dies")
    void resolvesForRemainingLegalTarget() {
        Permanent survivingBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dyingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(survivingBear.getId(), dyingBear.getId()));
        harness.castInstant(player2, 0, dyingBear.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivingBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, survivingBear, Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Rouse the Mob");
    }

    @Test
    @DisplayName("Any number of targets includes more than ninety-nine creatures")
    void canTargetOneHundredCreatures() {
        List<Permanent> bears = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            bears.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        }
        harness.setHand(player1, List.of(new RouseTheMob()));
        harness.addMana(player1, ManaColor.RED, 100);
        harness.addMana(player1, ManaColor.COLORLESS, 198);

        harness.castInstant(player1, 0, bears.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        for (Permanent bear : bears) {
            assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        }
    }
}
