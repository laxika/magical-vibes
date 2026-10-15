package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.Morphling;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CounterType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Humble.class, Morphling.class, ShivanHellkite.class, WornPowerstone.class})
class HumbleTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target creature 0/1 and removes its abilities")
    void makesTargetZeroOneWithoutAbilities() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player2, new ShivanHellkite());
        Permanent otherHellkite = harness.addToBattlefieldAndReturn(player2, new ShivanHellkite());
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.FLYING)).isTrue();

        castHumble(hellkite.getId());

        assertThat(gqs.getEffectivePower(gd, hellkite)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, hellkite)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, otherHellkite)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherHellkite)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, otherHellkite, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtCleanup() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player2, new ShivanHellkite());
        castHumble(hellkite.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, hellkite)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hellkite)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Base P/T changes still allow counters to apply")
    void countersApplyToNewBasePowerAndToughness() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player2, new ShivanHellkite());
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHumble(hellkite.getId());

        assertThat(gqs.getEffectivePower(gd, hellkite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hellkite)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes activated abilities as well as keyword abilities")
    void removesActivatedAbilities() {
        Permanent morphling = harness.addToBattlefieldAndReturn(player1, new Morphling());
        castHumble(morphling.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent powerstone = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.addToBattlefield(player1, new ShivanHellkite());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID powerstoneId = powerstone.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, powerstoneId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Removes flying granted before Humble resolves")
    void removesPreviouslyGrantedFlying() {
        Permanent morphling = harness.addToBattlefieldAndReturn(player1, new Morphling());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, morphling, Keyword.FLYING)).isTrue();

        castHumble(morphling.getId());

        assertThat(gqs.hasKeyword(gd, morphling, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, morphling)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, morphling)).isEqualTo(1);
    }

    @Test
    @DisplayName("An ability already on the stack can grant flying after Humble resolves")
    void pendingAbilityCanGrantFlyingAfterHumble() {
        Permanent morphling = harness.addToBattlefieldAndReturn(player1, new Morphling());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        castHumble(morphling.getId());
        assertThat(gqs.hasKeyword(gd, morphling, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, morphling, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, morphling)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, morphling)).isEqualTo(1);
    }

    @Test
    @DisplayName("A resolved power and toughness boost still applies after Humble")
    void existingBoostAppliesToNewBaseStats() {
        Permanent morphling = harness.addToBattlefieldAndReturn(player1, new Morphling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();

        castHumble(morphling.getId());

        assertThat(gqs.getEffectivePower(gd, morphling)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, morphling)).isEqualTo(2);
    }

    @Test
    @DisplayName("Humble does not affect a target that gains shroud in response")
    void targetGainingShroudMakesHumbleFailToResolve() {
        Permanent morphling = harness.addToBattlefieldAndReturn(player1, new Morphling());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, morphling.getId());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, morphling, Keyword.SHROUD)).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, morphling, Keyword.SHROUD)).isTrue();
        assertThat(gqs.getEffectivePower(gd, morphling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, morphling)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Humble");
        assertThat(gd.stack).isEmpty();
    }

    private void castHumble(UUID targetId) {
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
