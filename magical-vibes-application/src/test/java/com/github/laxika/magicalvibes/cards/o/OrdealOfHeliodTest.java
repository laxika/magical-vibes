package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.Auratog;
import com.github.laxika.magicalvibes.cards.r.RayOfDissolution;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
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

@CardUsed({OrdealOfHeliod.class, TravelingPhilosopher.class, RayOfDissolution.class, Auratog.class})
class OrdealOfHeliodTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking enchanted creature gets a +1/+1 counter")
    void attackPutsCounterOnEnchantedCreature() {
        Permanent creature = castOnCreature();

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ordeal of Heliod");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Third +1/+1 counter sacrifices the Aura and gains 10 life")
    void thirdCounterSacrificesAuraAndGainsLife() {
        Permanent creature = castOnCreature();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        attack(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Ordeal of Heliod");
        harness.assertInGraveyard(player1, "Ordeal of Heliod");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(30);
    }

    @Test
    @DisplayName("Destroying the Aura without sacrificing it does not gain life")
    void destructionDoesNotGainLife() {
        castOnCreature();
        Permanent aura = findPermanent(player1, "Ordeal of Heliod");

        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, aura.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertLife(player2, 23);
        harness.assertInGraveyard(player1, "Ordeal of Heliod");
    }

    @Test
    @DisplayName("Two counters are below the sacrifice threshold")
    void secondCounterDoesNotSacrificeAura() {
        Permanent creature = castOnCreature();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        attack(creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Ordeal of Heliod");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Existing counters do not sacrifice the Aura until the attack trigger resolves")
    void existingCountersStillRequireAttack() {
        Permanent creature = addCreatureReady(player1, new TravelingPhilosopher());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player1, List.of(new OrdealOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ordeal of Heliod");
        harness.assertLife(player1, 20);

        attack(creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Ordeal of Heliod");
        harness.assertLife(player1, 30);
    }

    @Test
    @DisplayName("An opponent's enchanted attacker gives life to the Aura's controller")
    void opponentCreatureTriggersAuraForItsController() {
        Permanent creature = addCreatureReady(player2, new TravelingPhilosopher());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new OrdealOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
            resolveAllTriggers();
        });

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Ordeal of Heliod");
        harness.assertNotOnBattlefield(player1, "Ordeal of Heliod");
        harness.assertLife(player1, 30);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop the attack trigger's counter")
    void attackTriggerSurvivesAuraDestruction() {
        Permanent creature = castOnCreature();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = findPermanent(player1, "Ordeal of Heliod");
        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.castAndResolveInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Ordeal of Heliod");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Sacrificing the Aura as another ability's cost also gains 10 life")
    void sacrificeAsAbilityCostGainsLifeWithoutAttack() {
        Permanent creature = castOnCreature();
        Permanent auratog = harness.addToBattlefieldAndReturn(player1, new Auratog());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(auratog), null, null);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Ordeal of Heliod");
        harness.assertInGraveyard(player1, "Ordeal of Heliod");
        harness.assertLife(player1, 30);
    }

    private Permanent castOnCreature() {
        Permanent creature = addCreatureReady(player1, new TravelingPhilosopher());

        harness.setHand(player1, List.of(new OrdealOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void attack(Permanent creature) {
        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        declareAttackers(List.of(creatureIndex));
        harness.passBothPriorities();
    }
}
