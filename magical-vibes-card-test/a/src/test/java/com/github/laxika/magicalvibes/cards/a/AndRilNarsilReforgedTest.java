package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AndRilNarsilReforged.class, GrizzlyBears.class})
class AndRilNarsilReforgedTest extends BaseCardTest {

    @Test
    void attackingWithEquippedCreaturePutsOneCounterOnEachControlledCreature() {
        Permanent equipment = addEquipment();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        equipment.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackingWithEquippedCreaturePutsTwoCountersWithCitysBlessing() {
        Permanent equipment = addEquipment();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(attacker.getId());
        gd.playersWithCityBlessing.add(player1.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void equipForThreeManaAttachesToControlledCreature() {
        Permanent equipment = addEquipment();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void attackingWithUnequippedCreatureDoesNotPutCounters() {
        Permanent equipment = addEquipment();
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(equippedCreature.getId());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(equippedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equipment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void ascendGrantsBlessingAtTenPermanentsAndItPersistsBelowTen() {
        Permanent equipment = addEquipment();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(attacker.getId());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.runStateBasedActions();
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());

        Permanent tenthPermanent = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tenthPermanent);
        harness.runStateBasedActions();

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(equipment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void blessingGainedAfterAttackUpgradesCountersAtResolution() {
        Permanent equipment = addEquipment();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(attacker.getId());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        Permanent newCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(newCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentAttackingWithEquippedCreatureCountersEquipmentControllersCreatures() {
        Permanent equipment = addEquipment();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        equipment.setAttachedTo(opponentAttacker.getId());
        gd.playersWithCityBlessing.add(player2.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackTriggerStillResolvesAfterEquipmentLeavesBattlefield() {
        Permanent equipment = addEquipment();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addEquipment() {
        return harness.addToBattlefieldAndReturn(player1, new AndRilNarsilReforged());
    }
}
