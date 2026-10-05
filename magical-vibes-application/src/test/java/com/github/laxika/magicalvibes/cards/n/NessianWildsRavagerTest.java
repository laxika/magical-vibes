package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NessianWildsRavager.class, NyxbornWolf.class})
class NessianWildsRavagerTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent pays tribute and the Ravager enters with six +1/+1 counters")
    void opponentPaysTribute() {
        castRavager();

        harness.handleMayAbilityChosen(player2, true);

        Permanent ravager = findPermanent(player1, "Nessian Wilds Ravager");
        assertThat(ravager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining tribute allows the Ravager to fight another creature")
    void opponentDeclinesTributeAndRavagerFights() {
        Permanent target = addCreature(player2);
        castRavager();

        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Nyxborn Wolf");
        assertThat(findPermanent(player1, "Nessian Wilds Ravager")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller may decline the fight after tribute is declined")
    void controllerDeclinesFight() {
        addCreature(player2);
        castRavager();

        harness.handleMayAbilityChosen(player2, false);
        Permanent target = findPermanent(player2, "Nyxborn Wolf");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Nyxborn Wolf");
        harness.assertOnBattlefield(player1, "Nessian Wilds Ravager");
    }

    @Test
    void decliningTributeWithNoOtherCreaturesDoesNotPromptForAFight() {
        castRavager();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Nessian Wilds Ravager")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void payingTributeDoesNotFightAnAvailableCreature() {
        Permanent target = addCreature(player2);
        castRavager();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Nyxborn Wolf");
        assertThat(findPermanent(player1, "Nessian Wilds Ravager")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void canFightAnotherCreatureControlledByItsController() {
        Permanent target = addCreature(player1);
        castRavager();

        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Nyxborn Wolf");
        assertThat(findPermanent(player1, "Nessian Wilds Ravager").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotChooseItselfForTheFight() {
        Permanent target = addCreature(player2);
        castRavager();
        harness.handleMayAbilityChosen(player2, false);

        Permanent ravager = findPermanent(player1, "Nessian Wilds Ravager");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ravager.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player2, "Nyxborn Wolf");
    }

    @Test
    void neitherCreatureDealsDamageIfRavagerLeavesBeforeResolution() {
        Permanent target = addCreature(player2);
        castRavager();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, target.getId());

        Permanent ravager = findPermanent(player1, "Nessian Wilds Ravager");
        gd.playerBattlefields.get(player1.getId()).remove(ravager);
        gd.playerGraveyards.get(player1.getId()).add(ravager.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Nyxborn Wolf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityDoesNotResolveIfItsOnlyTargetLeaves() {
        Permanent target = addCreature(player2);
        castRavager();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Nessian Wilds Ravager").getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NyxbornWolf());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castRavager() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NessianWildsRavager()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
