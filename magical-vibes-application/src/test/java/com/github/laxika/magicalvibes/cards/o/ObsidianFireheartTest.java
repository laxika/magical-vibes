package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObsidianFireheart.class, Mountain.class, KrakenHatchling.class, VampireHexmage.class})
class ObsidianFireheartTest extends BaseCardTest {

    @Test
    @DisplayName("Ability puts a blaze counter on a land and burns its controller during upkeep")
    void putsBlazeCounterAndBurnsLandController() {
        Permanent fireheart = addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(mountain.getCounterCount(CounterType.BLAZE)).isEqualTo(1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(fireheart).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Burning ability persists after Obsidian Fireheart leaves the battlefield")
    void burningAbilityPersistsAfterSourceLeaves() {
        Permanent fireheart = addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, mountain.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(fireheart);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a land that already has a blaze counter")
    void cannotTargetLandWithBlazeCounter() {
        addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.setCounterCount(CounterType.BLAZE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addReadyFireheart(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KrakenHatchling());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing the last blaze counter ends the granted ability")
    void stopsBurningAfterAllBlazeCountersAreRemoved() {
        addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        addCreatureReady(player1, new VampireHexmage());
        ignite(mountain);

        harness.activateAbility(player1, 1, null, mountain.getId());
        harness.passBothPriorities();
        assertThat(mountain.getCounterCount(CounterType.BLAZE)).isZero();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing counters after the upkeep trigger does not stop that trigger")
    void alreadyTriggeredDamageSurvivesCounterRemoval() {
        addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        addCreatureReady(player2, new VampireHexmage());
        ignite(mountain);

        advanceToUpkeep(player2);
        harness.activateAbility(player2, 1, null, mountain.getId());
        harness.passBothPriorities();
        assertThat(mountain.getCounterCount(CounterType.BLAZE)).isZero();
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A land becoming illegal before resolution receives no blaze counter")
    void rechecksBlazeCounterRestrictionOnResolution() {
        addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, mountain.getId());
        mountain.setCounterCount(CounterType.BLAZE, 1);
        harness.passBothPriorities();

        assertThat(mountain.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple blaze counters still cause only one damage each upkeep")
    void extraBlazeCountersDoNotIncreaseDamage() {
        addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        ignite(mountain);
        mountain.setCounterCount(CounterType.BLAZE, 3);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Opponent's burning land does not trigger during Fireheart's controller's upkeep")
    void onlyTriggersOnLandControllersUpkeep() {
        addReadyFireheart(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        ignite(mountain);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A summoning-sick Fireheart can burn its own controller's land")
    void canActivateImmediatelyAndTargetOwnLand() {
        harness.addToBattlefield(player1, new ObsidianFireheart());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        ignite(mountain);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    private void ignite(Permanent land) {
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        assertThat(land.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
    }

    private Permanent addReadyFireheart(Player player) {
        return addCreatureReady(player, new ObsidianFireheart());
    }
}
