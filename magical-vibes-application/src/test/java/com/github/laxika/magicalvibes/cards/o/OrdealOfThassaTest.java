package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QuietPurity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrdealOfThassaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking enchanted creature gets a +1/+1 counter")
    void attackPutsCounterOnEnchantedCreature() {
        Permanent creature = castOnGrizzlyBears();

        attack(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ordeal of Thassa");
    }

    @Test
    @DisplayName("Third +1/+1 counter sacrifices the Aura and draws two cards")
    void thirdCounterSacrificesAuraAndDrawsTwoCards() {
        Permanent creature = castOnGrizzlyBears();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

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
        castOnGrizzlyBears();
        Permanent aura = findPermanent(player1, "Ordeal of Thassa");
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new QuietPurity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertInGraveyard(player1, "Ordeal of Thassa");
    }

    private Permanent castOnGrizzlyBears() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new OrdealOfThassa()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void attack(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        gs.declareAttackers(gd, player1, List.of(creatureIndex));
        harness.passBothPriorities();
    }
}
