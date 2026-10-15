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

@CardUsed({OrdealOfThassa.class, BronzeSable.class, RayOfDissolution.class})
class OrdealOfThassaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking enchanted creature gets a +1/+1 counter")
    void attackPutsCounterOnEnchantedCreature() {
        Permanent creature = castOnBronzeSable();

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ordeal of Thassa");
    }

    @Test
    @DisplayName("Third +1/+1 counter sacrifices the Aura and draws two cards")
    void thirdCounterSacrificesAuraAndDrawsTwoCards() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new BronzeSable(), new BronzeSable()));

        attack(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Ordeal of Thassa");
        harness.assertInGraveyard(player1, "Ordeal of Thassa");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Destroying the Aura without sacrificing it does not draw cards")
    void destructionDoesNotDrawCards() {
        castOnBronzeSable();
        Permanent aura = findPermanent(player1, "Ordeal of Thassa");
        harness.setLibrary(player1, List.of(new BronzeSable(), new BronzeSable()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, aura.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertInGraveyard(player1, "Ordeal of Thassa");
    }

    @Test
    void secondCounterDoesNotSacrificeAuraOrDraw() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new BronzeSable(), new BronzeSable()));

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Ordeal of Thassa");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void existingCountersOnlyCauseSacrificeWhenAttackTriggerResolves() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setLibrary(player1, List.of(new BronzeSable(), new BronzeSable()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ordeal of Thassa");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        attack(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Ordeal of Thassa");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void destroyingAuraInResponseDoesNotStopCounterPlacement() {
        Permanent creature = castOnBronzeSable();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = findPermanent(player1, "Ordeal of Thassa");
        harness.setLibrary(player1, List.of(new BronzeSable(), new BronzeSable()));
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Ordeal of Thassa");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void enchantingOpponentsCreatureDrawsForAuraController() {
        Permanent creature = addCreatureReady(player2, new BronzeSable());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new OrdealOfThassa()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new BronzeSable(), new BronzeSable()));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Ordeal of Thassa");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    private Permanent castOnBronzeSable() {
        Permanent creature = addCreatureReady(player1, new BronzeSable());

        harness.setHand(player1, List.of(new OrdealOfThassa()));
        harness.addMana(player1, ManaColor.BLUE, 2);

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
