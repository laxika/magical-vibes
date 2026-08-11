package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlenElendraGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a -1/-1 counter")
    void entersWithMinusOneMinusOneCounter() {
        harness.setHand(player1, List.of(new GlenElendraGuardian()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent guardian = findPermanent(player1, "Glen Elendra Guardian");
        assertThat(guardian.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes a counter to counter a noncreature spell and its controller draws")
    void countersNoncreatureSpellAndItsControllerDraws() {
        Permanent guardian = addReadyGuardian();
        guardian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());

        assertThat(guardian.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Permanent guardian = addReadyGuardian();
        guardian.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        LlanowarElves elves = new LlanowarElves();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGuardian() {
        return addCreatureReady(player1, new GlenElendraGuardian());
    }
}
