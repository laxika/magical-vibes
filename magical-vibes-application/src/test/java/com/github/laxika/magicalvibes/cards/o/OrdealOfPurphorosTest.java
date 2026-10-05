package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.r.RayOfDissolution;
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

@CardUsed({OrdealOfPurphoros.class, TravelingPhilosopher.class, RayOfDissolution.class})
class OrdealOfPurphorosTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking enchanted creature gets a +1/+1 counter")
    void attackPutsCounterOnEnchantedCreature() {
        Permanent creature = castOnTravelingPhilosopher();

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ordeal of Purphoros");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Third +1/+1 counter sacrifices the Aura and deals 3 damage to a target")
    void thirdCounterSacrificesAuraAndDealsDamage() {
        Permanent creature = castOnTravelingPhilosopher();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        attack(creature);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Ordeal of Purphoros");
        harness.assertInGraveyard(player1, "Ordeal of Purphoros");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Destroying the Aura without sacrificing it does not deal damage")
    void destructionDoesNotDealDamage() {
        castOnTravelingPhilosopher();
        Permanent aura = findPermanent(player1, "Ordeal of Purphoros");

        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, aura.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
        harness.assertInGraveyard(player1, "Ordeal of Purphoros");
    }

    @Test
    @DisplayName("More than three counters also sacrifices the Aura, and damage can target a creature")
    void aboveThresholdCanDamageCreature() {
        Permanent creature = castOnTravelingPhilosopher();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent target = addCreatureReady(player1, new TravelingPhilosopher());

        attack(creature);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Ordeal of Purphoros");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Traveling Philosopher");
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop the attack counter")
    void attackTriggerSurvivesAuraDestruction() {
        Permanent creature = castOnTravelingPhilosopher();
        Permanent aura = findPermanent(player1, "Ordeal of Purphoros");
        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Ordeal of Purphoros");
    }

    private Permanent castOnTravelingPhilosopher() {
        Permanent creature = addCreatureReady(player1, new TravelingPhilosopher());

        harness.setHand(player1, List.of(new OrdealOfPurphoros()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void attack(Permanent creature) {
        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        declareAttackers(player1, List.of(creatureIndex));
        harness.passBothPriorities();
    }
}
