package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
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

@CardUsed({BloodLust.class, BenalishHero.class, GrizzlyBears.class, HowlingMine.class,
        WallOfSwords.class, WallOfStone.class})
class BloodLustTest extends BaseCardTest {

    @Test
    @DisplayName("Toughness below 5: drops to 1 and gains +4 power (2/2 -> 6/1)")
    void smallToughnessDropsToOne() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloodLust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloodLust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Toughness greater than 5: gets +4/-4 (0/8 -> 4/4)")
    void largeToughnessLosesFour() {
        harness.addToBattlefield(player1, new WallOfStone());
        harness.setHand(player1, List.of(new BloodLust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID wallId = harness.getPermanentId(player1, "Wall of Stone");
        harness.castAndResolveInstant(player1, 0, wallId);

        Permanent wall = findPermanent(player1, "Wall of Stone");
        assertThat(wall.getEffectivePower()).isEqualTo(4);
        assertThat(wall.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Toughness exactly 5: gets +4/-4 (3/5 -> 7/1)")
    void exactThresholdLosesFour() {
        harness.addToBattlefield(player1, new WallOfSwords());
        harness.setHand(player1, List.of(new BloodLust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID wallId = harness.getPermanentId(player1, "Wall of Swords");
        harness.castAndResolveInstant(player1, 0, wallId);

        Permanent wall = findPermanent(player1, "Wall of Swords");
        assertThat(wall.getEffectivePower()).isEqualTo(7);
        assertThat(wall.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Toughness 1: gets +4/+0 because X is zero (1/1 -> 5/1)")
    void oneToughnessLosesNothing() {
        harness.addToBattlefield(player1, new BenalishHero());
        harness.setHand(player1, List.of(new BloodLust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID heroId = harness.getPermanentId(player1, "Benalish Hero");
        harness.castAndResolveInstant(player1, 0, heroId);

        Permanent hero = findPermanent(player1, "Benalish Hero");
        assertThat(hero.getEffectivePower()).isEqualTo(5);
        assertThat(hero.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blood Lust wears off at cleanup step")
    void wearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloodLust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Blood Lust")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of(new BloodLust()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Howling Mine");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
