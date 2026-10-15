package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
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

@CardUsed({OrdealOfErebos.class, BronzeSable.class, RayOfDissolution.class})
class OrdealOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking enchanted creature gets a +1/+1 counter")
    void attackPutsCounterOnEnchantedCreature() {
        Permanent creature = castOnBronzeSable();

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ordeal of Erebos");
    }

    @Test
    @DisplayName("Third +1/+1 counter sacrifices the Aura and makes a target player discard two cards")
    void thirdCounterSacrificesAuraAndDiscardsTwoCards() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new BronzeSable(), new BronzeSable()));

        attack(creature);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Ordeal of Erebos");
        harness.assertInGraveyard(player1, "Ordeal of Erebos");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroying the Aura without sacrificing it does not discard cards")
    void destructionDoesNotDiscardCards() {
        castOnBronzeSable();
        Permanent aura = findPermanent(player1, "Ordeal of Erebos");

        harness.setHand(player2, List.of(new RayOfDissolution(), new BronzeSable(), new BronzeSable()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ordeal of Erebos");
    }

    @Test
    @DisplayName("Two counters after the attack do not sacrifice the Aura")
    void secondCounterDoesNotSacrificeAura() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Ordeal of Erebos");
    }

    @Test
    @DisplayName("Existing counters do not sacrifice the Aura until its attack trigger resolves")
    void existingThreeCountersWaitForAttackAndSacrificeAboveThree() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player2, List.of());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ordeal of Erebos");

        attack(creature);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Ordeal of Erebos");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice trigger can target its controller, discarding as much as possible")
    void sacrificeCanTargetControllerWithOneCard() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new BronzeSable()));

        attack(creature);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bronze Sable");
        harness.assertInGraveyard(player1, "Ordeal of Erebos");
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop the attack counter")
    void attackCounterStillResolvesAfterAuraIsDestroyed() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = findPermanent(player1, "Ordeal of Erebos");
        harness.setHand(player1, List.of(new RayOfDissolution(), new BronzeSable()));
        harness.setHand(player2, List.of(new BronzeSable(), new BronzeSable()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Ordeal of Erebos");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing enchanted attacker gets counters while the Aura controller chooses the discard target")
    void opposingEnchantedAttackerTriggersAuraForItsController() {
        Permanent creature = addCreatureReady(player2, new BronzeSable());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new OrdealOfErebos()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new BronzeSable(), new BronzeSable()));

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Ordeal of Erebos");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private Permanent castOnBronzeSable() {
        Permanent creature = addCreatureReady(player1, new BronzeSable());

        harness.setHand(player1, List.of(new OrdealOfErebos()));
        harness.addMana(player1, ManaColor.BLACK, 2);

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
