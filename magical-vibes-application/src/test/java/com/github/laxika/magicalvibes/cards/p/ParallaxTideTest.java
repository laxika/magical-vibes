package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParallaxTide.class, KorHaven.class, Mossdog.class, SealOfCleansing.class})
class ParallaxTideTest extends BaseCardTest {

    private void castAndResolveTide() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ParallaxTide(), "{2}{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Parallax Tide enters with five fade counters")
    void entersWithFiveFadeCounters() {
        castAndResolveTide();

        Permanent tide = findPermanent(player1, "Parallax Tide");
        assertThat(tide.getCounterCount(CounterType.FADE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Parallax Tide removes a fade counter at upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(tide.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Parallax Tide");
    }

    @Test
    @DisplayName("Parallax Tide removes its last fade counter without sacrificing")
    void removesLastFadeCounterWithoutSacrificing() {
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(tide.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Parallax Tide");
    }

    @Test
    @DisplayName("Parallax Tide sacrifices itself when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        harness.addToBattlefield(player1, new ParallaxTide());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Parallax Tide");
    }

    @Test
    @DisplayName("Parallax Tide exiles a target land and returns it when Tide leaves")
    void exilesLandUntilTideLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KorHaven());
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kor Haven");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Kor Haven"));
        assertThat(tide.getCounterCount(CounterType.FADE)).isZero();

        harness.addToBattlefield(player2, new SealOfCleansing());
        harness.activateAbility(player2, 0, null, tide.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Kor Haven");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Kor Haven"));
    }

    @Test
    @DisplayName("Parallax Tide cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Mossdog());
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Parallax Tide cannot activate without a fade counter")
    void cannotActivateWithoutFadeCounter() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KorHaven());
        harness.addToBattlefield(player1, new ParallaxTide());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Parallax Tide returns exiled lands to their owners")
    void returnsExiledLandsToTheirOwners() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new KorHaven());
        tide.setCounterCount(CounterType.FADE, 2);

        harness.activateAbility(player1, 1, null, ownLand.getId());
        harness.activateAbility(player1, 1, null, opponentLand.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Kor Haven");
        harness.assertNotOnBattlefield(player2, "Kor Haven");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Kor Haven"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Kor Haven"));

        harness.addToBattlefield(player2, new SealOfCleansing());
        harness.activateAbility(player2, 0, null, tide.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Kor Haven")).hasSize(1);
        assertThat(findPermanents(player2, "Kor Haven")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Kor Haven"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Kor Haven"));
    }

    @Test
    @DisplayName("Parallax Tide's exile ability still resolves after Tide leaves before resolution")
    void exileAbilityResolvesAfterTideLeavesBeforeResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KorHaven());
        harness.addToBattlefield(player2, new SealOfCleansing());
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 1, null, tide.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Parallax Tide");
        harness.assertNotOnBattlefield(player2, "Kor Haven");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Kor Haven"));
    }
}
