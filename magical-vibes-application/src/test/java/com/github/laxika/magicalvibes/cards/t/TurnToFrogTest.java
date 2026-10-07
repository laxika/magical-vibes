package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.f.Flight;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({TurnToFrog.class, SerraAngel.class, Manalith.class, RuneclawBear.class, TitanicGrowth.class, Flight.class, RoyalAssassin.class})
class TurnToFrogTest extends BaseCardTest {

    @Test
    @DisplayName("Sets the target creature's base power and toughness to 1/1")
    void makesTargetOneOne() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // 4/4

        castTurnToFrog(angel.getId());

        assertThat(angel.getEffectivePower()).isEqualTo(1);
        assertThat(angel.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Strips the target creature's abilities")
    void stripsAbilities() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // flying, vigilance
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();

        castTurnToFrog(angel.getId());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Turns the target creature blue, replacing its other colors, and into a Frog")
    void becomesBlueFrog() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // white
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();

        castTurnToFrog(angel.getId());

        assertThat(gqs.hasColor(gd, angel, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isFalse();
        assertThat(angel.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.FROG);
    }

    @Test
    @DisplayName("All effects wear off at end of turn")
    void wearsOffAtCleanup() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castTurnToFrog(angel.getId());

        assertThat(angel.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(angel.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new Manalith());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID fountainId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castTurnToFrog(UUID targetId) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    void retainsPowerAndToughnessBoostAppliedBeforeTurningIntoFrog() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        castTurnToFrog(bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    @Test
    void appliesPowerAndToughnessBoostAfterTurningIntoFrog() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castTurnToFrog(bear.getId());

        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    @Test
    void retainsPlusOnePlusOneCounters() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castTurnToFrog(bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @CardUsed({Xenograft.class})
    void laterCreatureTypeGrantAddsToFrogType() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castTurnToFrog(bear.getId());

        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.FROG)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.BEAR)).isFalse();
    }

    @Test
    @CardUsed({Xenograft.class})
    void replacesEarlierGrantedCreatureType() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.GOBLIN)).isTrue();

        castTurnToFrog(bear.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.FROG)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.BEAR)).isFalse();
    }

    @Test
    void removesEarlierGrantedFlying() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Flight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();

        castTurnToFrog(bear.getId());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        harness.assertOnBattlefield(player1, "Flight");
    }

    @Test
    void retainsLaterGrantedFlying() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castTurnToFrog(bear.getId());

        harness.setHand(player1, List.of(new Flight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    void preventsActivatingRemovedAbility() {
        Permanent assassin = addCreatureReady(player1, new RoyalAssassin());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.tap();

        castTurnToFrog(assassin.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCounterAbilityAlreadyOnStack() {
        Permanent assassin = addCreatureReady(player1, new RoyalAssassin());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.tap();
        harness.activateAbility(player1, 0, null, bear.getId());

        castTurnToFrog(assassin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }
}
