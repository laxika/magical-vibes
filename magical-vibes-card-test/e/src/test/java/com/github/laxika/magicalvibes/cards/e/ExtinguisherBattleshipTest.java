package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StationMonitor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtinguisherBattleship.class, Forest.class, StationMonitor.class})
class ExtinguisherBattleshipTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it destroys a noncreature permanent and deals 4 damage to each creature")
    void entersDestroysPermanentAndDamagesCreatures() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new StationMonitor());
        harness.addToBattlefield(player2, new StationMonitor());

        castBattleship(forest.getId());

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Station Monitor");
        harness.assertNotOnBattlefield(player2, "Station Monitor");
        harness.assertOnBattlefield(player1, "Extinguisher Battleship");
    }

    @Test
    @DisplayName("The entry ability deals exactly four damage to surviving creatures and none to players")
    void entryDealsFourDamageWithoutDamagingPlayers() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StationMonitor());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castBattleship(forest.getId());

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Station Monitor");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature with its enter-the-battlefield ability")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new StationMonitor());
        harness.setHand(player1, List.of(new ExtinguisherBattleship()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1,
                new ExtinguisherBattleship());
        Permanent creature = addCreatureReady(player1, new StationMonitor());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(battleship), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(battleship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Five charge counters make it an artifact creature with flying and trample")
    void fiveChargeCountersUnlockAbilities() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1,
                new ExtinguisherBattleship());

        battleship.setCounterCount(CounterType.CHARGE, 4);
        assertThat(gqs.isCreature(gd, battleship)).isFalse();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.TRAMPLE)).isFalse();

        battleship.setCounterCount(CounterType.CHARGE, 5);
        assertThat(gqs.isCreature(gd, battleship)).isTrue();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Station can tap a summoning-sick creature and uses its power at resolution")
    void stationUsesCurrentPowerOfSummoningSickCreature() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1, new ExtinguisherBattleship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StationMonitor());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(battleship), null, null);
        assertThat(creature.isTapped()).isTrue();
        assertThat(battleship.getCounterCount(CounterType.CHARGE)).isZero();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(battleship.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(gqs.isCreature(gd, battleship)).isTrue();
    }

    @Test
    @DisplayName("An animated Battleship cannot tap itself to station")
    void cannotStationWithItself() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1, new ExtinguisherBattleship());
        battleship.setCounterCount(CounterType.CHARGE, 5);
        battleship.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(battleship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(battleship.isTapped()).isFalse();
        assertThat(battleship.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Station cannot tap an opponent's creature or an already tapped creature")
    void stationRequiresUntappedCreatureYouControl() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1, new ExtinguisherBattleship());
        harness.addToBattlefield(player2, new StationMonitor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StationMonitor());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(battleship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(battleship.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Station cannot be activated outside a main phase")
    void stationOnlyAtSorcerySpeed() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1, new ExtinguisherBattleship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StationMonitor());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(battleship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Dropping below five charge counters removes animation, flying, and trample")
    void losingCountersRemovesStationAbilities() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1, new ExtinguisherBattleship());
        battleship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, battleship)).isTrue();

        battleship.setCounterCount(CounterType.CHARGE, 4);

        assertThat(gqs.isCreature(gd, battleship)).isFalse();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("If the entry ability's target becomes a creature, none of the ability resolves")
    void illegalEntryTargetPreventsCreatureDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExtinguisherBattleship());
        harness.addToBattlefield(player1, new StationMonitor());
        harness.addToBattlefield(player2, new StationMonitor());
        harness.setHand(player1, List.of(new ExtinguisherBattleship()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();

        target.setCounterCount(CounterType.CHARGE, 5);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Extinguisher Battleship");
        harness.assertOnBattlefield(player1, "Station Monitor");
        harness.assertOnBattlefield(player2, "Station Monitor");
    }

    private void castBattleship(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ExtinguisherBattleship()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castArtifact(player1, 0, targetId);
        resolveAllTriggers();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
