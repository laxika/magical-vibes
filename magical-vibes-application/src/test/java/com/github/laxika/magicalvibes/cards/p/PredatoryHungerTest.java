package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PredatoryHungerTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a creature spell puts a +1/+1 counter on the enchanted creature")
    void opponentCastingCreatureSpellAddsCounter() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        enchantHost(host);

        castOpponentCreatureSpell();
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not trigger Predatory Hunger")
    void opponentCastingNoncreatureSpellDoesNotAddCounter() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        enchantHost(host);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller casting a creature spell does not trigger Predatory Hunger")
    void controllerCastingCreatureSpellDoesNotAddCounter() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        enchantHost(host);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void enchantHost(Permanent host) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PredatoryHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
    }

    private void castOpponentCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
    }
}
