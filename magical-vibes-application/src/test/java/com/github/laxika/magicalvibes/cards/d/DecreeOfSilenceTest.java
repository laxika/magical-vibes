package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecreeOfSilence.class, Shock.class, SerraAngel.class})
class DecreeOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent's spell and gets a depletion counter")
    void countersOpponentSpellAndGetsDepletionCounter() {
        Permanent decree = harness.addToBattlefieldAndReturn(player1, new DecreeOfSilence());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(decree.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Decree of Silence");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Sacrifices itself after receiving its third depletion counter")
    void sacrificesAfterThirdDepletionCounter() {
        Permanent decree = harness.addToBattlefieldAndReturn(player1, new DecreeOfSilence());
        decree.setCounterCount(CounterType.DEPLETION, 2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Decree of Silence");
        harness.assertInGraveyard(player1, "Decree of Silence");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Does not trigger for a spell its controller casts")
    void doesNotTriggerForControllerSpell() {
        Permanent decree = harness.addToBattlefieldAndReturn(player1, new DecreeOfSilence());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(decree.getCounterCount(CounterType.DEPLETION)).isZero();
        harness.assertOnBattlefield(player1, "Decree of Silence");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Cycling may counter a spell and still draws")
    void cyclingMayCounterSpellAndStillDraws() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(new DecreeOfSilence()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Decree of Silence");
        harness.assertInHand(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Declining the cycling counter still draws")
    void decliningCyclingCounterStillDraws() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(new DecreeOfSilence()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Decree of Silence");
        harness.assertInHand(player1, "Serra Angel");
    }
}
