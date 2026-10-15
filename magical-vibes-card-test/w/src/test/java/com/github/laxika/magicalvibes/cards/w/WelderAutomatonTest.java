package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WelderAutomaton.class, Shock.class})
class WelderAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent without damaging its controller")
    void dealsDamageToEachOpponent() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WelderAutomaton());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(automaton), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Requires three generic and one red mana")
    void requiresMana() {
        harness.addToBattlefield(player1, new WelderAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresFullGenericManaPayment() {
        harness.addToBattlefield(player1, new WelderAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WelderAutomaton());
        automaton.tap();
        automaton.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(automaton.isTapped()).isTrue();
    }

    @Test
    void canActivateRepeatedlyWithEachActivationPaid() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WelderAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(automaton.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityResolvesAfterSourceIsDestroyed() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WelderAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, automaton.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(automaton);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
