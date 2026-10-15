package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToadstoolAdmirer.class, TorchTheTower.class})
class ToadstoolAdmirerTest extends BaseCardTest {

    @Test
    void activatedAbilityPutsPlusOnePlusOneCounterOnIt() {
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new ToadstoolAdmirer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(admirer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityDoesNotRequireTapping() {
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new ToadstoolAdmirer());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(admirer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(admirer.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityCanBeUsedWhileTapped() {
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new ToadstoolAdmirer());
        admirer.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(admirer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(admirer.isTapped()).isTrue();
    }

    @Test
    void wardCountersOpponentSpellWhenTheyCannotPay() {
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new ToadstoolAdmirer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TorchTheTower()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, admirer.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Toadstool Admirer");
        harness.assertInGraveyard(player2, "Torch the Tower");
        assertThat(admirer.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardCountersOpponentSpellWhenTheyDeclinePayment() {
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new ToadstoolAdmirer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TorchTheTower()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, admirer.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Toadstool Admirer");
        harness.assertInGraveyard(player2, "Torch the Tower");
        assertThat(admirer.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingWardLetsOpponentSpellResolve() {
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new ToadstoolAdmirer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TorchTheTower()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, admirer.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Toadstool Admirer");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(admirer.getCard().getId()));
        harness.assertInGraveyard(player2, "Torch the Tower");
    }

    @Test
    void wardDoesNotTriggerForControllersSpell() {
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new ToadstoolAdmirer());
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, admirer.getId());

        harness.assertNotOnBattlefield(player1, "Toadstool Admirer");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(admirer.getCard().getId()));
        assertThat(gd.stack).isEmpty();
    }
}
