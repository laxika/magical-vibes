package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cloudskate;
import com.github.laxika.magicalvibes.cards.m.ManaCache;
import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParallaxWave.class, Cloudskate.class, ManaCache.class, SealOfCleansing.class, SoulWarden.class})
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
        harness.activateAbility(player2, 0, null, wave.getId());
        resolveAllTriggers();

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
        harness.activateAbility(player2, 0, null, wave.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Parallax Wave");
        harness.assertNotOnBattlefield(player2, "Cloudskate");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Cloudskate"));
    }

    @Test
    @DisplayName("Creatures remain exiled until Parallax Wave's leaves trigger resolves")
    void returnWaitsForLeavesTriggerResolution() {
        harness.addToBattlefield(player2, new SealOfCleansing());
        Permanent cloudskate = addCreatureReady(player2, new Cloudskate());
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, cloudskate.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, wave.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Parallax Wave");
        harness.assertNotOnBattlefield(player2, "Cloudskate");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Cloudskate"));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cloudskate");
    }

    @Test
    @DisplayName("Removing the last fade counter at upkeep does not sacrifice Parallax Wave")
    void lastFadeCounterDoesNotCauseImmediateSacrifice() {
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(wave.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Parallax Wave");

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Parallax Wave");
        harness.assertInGraveyard(player1, "Parallax Wave");
    }

    @Test
    @DisplayName("Parallax Wave does not fade during its opponent's upkeep")
    void doesNotFadeDuringOpponentsUpkeep() {
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(wave.getCounterCount(CounterType.FADE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Parallax Wave");
    }

    @Test
    @DisplayName("Fading returns creatures exiled by Parallax Wave to both players")
    void fadingReturnsAllExiledCreatures() {
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 2);
        Permanent ownCreature = addCreatureReady(player1, new Cloudskate());
        Permanent opposingCreature = addCreatureReady(player2, new Cloudskate());

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, opposingCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cloudskate");
        harness.assertNotOnBattlefield(player2, "Cloudskate");

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Parallax Wave");
        harness.assertOnBattlefield(player1, "Cloudskate");
        harness.assertOnBattlefield(player2, "Cloudskate");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures returning with Soul Warden enter simultaneously")
    void returnsExiledCreaturesSimultaneously() {
        Permanent wave = harness.addToBattlefieldAndReturn(player1, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 2);
        Permanent cloudskate = addCreatureReady(player1, new Cloudskate());
        Permanent warden = addCreatureReady(player1, new SoulWarden());

        harness.activateAbility(player1, 0, null, cloudskate.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, warden.getId());
        resolveAllTriggers();
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cloudskate");
        harness.assertOnBattlefield(player1, "Soul Warden");
        harness.assertLife(player1, 21);
    }
}
