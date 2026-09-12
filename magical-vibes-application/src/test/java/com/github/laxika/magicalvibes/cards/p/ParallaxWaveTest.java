package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cloudskate;
import com.github.laxika.magicalvibes.cards.m.ManaCache;
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

@CardUsed({ParallaxWave.class, Cloudskate.class, ManaCache.class, SealOfCleansing.class})
class ParallaxWaveTest extends BaseCardTest {

    private void castAndResolveWave() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ParallaxWave(), "{2}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Parallax Wave enters with five fade counters")
    void entersWithFiveFadeCounters() {
        castAndResolveWave();

        Permanent wave = findPermanent(player1, "Parallax Wave");
        assertThat(wave.getCounterCount(CounterType.FADE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Parallax Wave removes a fade counter at upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wave.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Parallax Wave");
    }

    @Test
    @DisplayName("Parallax Wave sacrifices itself when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Parallax Wave");
    }

    @Test
    @DisplayName("Parallax Wave exiles a target creature and returns it when Wave leaves")
    void exilesCreatureUntilWaveLeaves() {
        harness.addToBattlefield(player2, new SealOfCleansing());
        Permanent cloudskate = addCreatureReady(player2, new Cloudskate());
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, cloudskate.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloudskate");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Cloudskate"));
        assertThat(wave.getCounterCount(CounterType.FADE)).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, wave.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cloudskate");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Cloudskate"));
    }

    @Test
    @DisplayName("Parallax Wave cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent cache = harness.addToBattlefieldAndReturn(player2, new ManaCache());
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cache.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Parallax Wave cannot activate without a fade counter")
    void cannotActivateWithoutFadeCounter() {
        Permanent cloudskate = addCreatureReady(player2, new Cloudskate());
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cloudskate.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Parallax Wave's exile ability still resolves after Wave leaves before resolution")
    void exileAbilityResolvesAfterWaveLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new SealOfCleansing());
        Permanent cloudskate = addCreatureReady(player2, new Cloudskate());
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, cloudskate.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, wave.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Parallax Wave");
        harness.assertNotOnBattlefield(player2, "Cloudskate");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Cloudskate"));
    }

}
