package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CatOwl;
import com.github.laxika.magicalvibes.cards.z.ZoeticGlyph;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({InvasionSubmersible.class, CatOwl.class, Island.class, ZoeticGlyph.class})
class InvasionSubmersibleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by returning up to one other nonland permanent to its owner's hand")
    void entersAndReturnsAnotherNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CatOwl());
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(target.getOriginalCard().getId()));
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Can enter without choosing an optional target")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Invasion Submersible");
    }

    @Test
    @DisplayName("The enter-the-battlefield target must be another nonland permanent")
    void rejectsLandTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("Waterbend permanently animates the Vehicle and adds three +1/+1 counters")
    void waterbendAnimatesAndAddsCounters() {
        Permanent submersible = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CatOwl());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CatOwl());

        harness.activateAbility(player1, 0, null, null);

        assertThat(submersible.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.isArtifact(submersible)).isTrue();
        assertThat(gqs.isCreature(gd, submersible)).isTrue();
        assertThat(submersible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, submersible)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, submersible)).isEqualTo(3);
    }

    @Test
    @DisplayName("Waterbend cannot be activated more than once")
    void waterbendCanBeActivatedOnlyOnce() {
        harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        harness.addToBattlefieldAndReturn(player1, new CatOwl());
        harness.addToBattlefieldAndReturn(player1, new CatOwl());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Can decline to bounce even when a legal target exists")
    void canDeclineAvailableTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CatOwl());
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertOnBattlefield(player1, "Invasion Submersible");
    }

    @Test
    @DisplayName("Can bounce another noncreature artifact controlled by its controller")
    void canBounceOwnNoncreatureArtifact() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(other);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(other.getCard());
        assertThat(countPermanents(player1, "Invasion Submersible")).isEqualTo(1);
    }

    @Test
    @DisplayName("Can pay entirely with mana even while the Vehicle is tapped")
    void tappedVehicleCanWaterbendWithMana() {
        Permanent submersible = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        submersible.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, submersible)).isTrue();
        assertThat(submersible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(submersible.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterbend can combine mana with tapping a summoning-sick artifact")
    void waterbendCombinesManaAndTapping() {
        Permanent submersible = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(submersible.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, submersible)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, submersible)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animation and counters remain after turn cleanup")
    void animationSurvivesCleanup() {
        Permanent submersible = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, submersible)).isTrue();
        assertThat(gqs.isCreature(gd, submersible)).isTrue();
        assertThat(submersible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, submersible)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, submersible)).isEqualTo(3);
    }

    @Test
    @DisplayName("A failed payment does not consume the exhaust activation")
    void failedPaymentDoesNotConsumeExhaust() {
        Permanent submersible = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(submersible.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, submersible)).isFalse();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(submersible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A bounced and recast Vehicle may exhaust again and loses its old animation and counters")
    void canExhaustAgainAfterLeavingBattlefield() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new InvasionSubmersible()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castArtifact(player1, 0, original.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(original.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @CardUsed({InvasionSubmersible.class, ZoeticGlyph.class})
    @DisplayName("Exhaust preserves base power and toughness established by an earlier effect")
    void exhaustPreservesExistingBasePowerAndToughness() {
        Permanent submersible = harness.addToBattlefieldAndReturn(player1, new InvasionSubmersible());
        harness.setHand(player1, List.of(new ZoeticGlyph()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, submersible.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, submersible)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, submersible)).isEqualTo(4);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(submersible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, submersible)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, submersible)).isEqualTo(7);
    }
}
