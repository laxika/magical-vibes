package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaringFiendbonder.class, GrizzlyBears.class, Mountain.class})
class DaringFiendbonderTest extends BaseCardTest {

    @Test
    @DisplayName("Daring Fiendbonder must attack each combat if able")
    void mustAttackEachCombatIfAble() {
        addCreatureReady(player1, new DaringFiendbonder());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Its graveyard ability puts an indestructible counter on target creature")
    void putsIndestructibleCounterOnTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyGraveyardAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its graveyard ability exiles Daring Fiendbonder as a cost")
    void exilesSourceAsCost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyGraveyardAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Daring Fiendbonder");
    }

    @Test
    @DisplayName("Its graveyard ability requires a creature target")
    void requiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyGraveyardAbility();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its graveyard ability can only be activated as a sorcery")
    void isSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyGraveyardAbility();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mustAttackEvenWhenItEnteredThisTurn() {
        harness.addToBattlefield(player1, new DaringFiendbonder());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void tappedFiendbonderDoesNotHaveToAttack() {
        Permanent fiendbonder = addCreatureReady(player1, new DaringFiendbonder());
        fiendbonder.tap();

        declareAttackers(List.of());

        assertThat(fiendbonder.isAttacking()).isFalse();
    }

    @Test
    void canPutCounterOnOpponentsCreatureDuringPostcombatMainPhase() {
        Permanent target = addCreatureReady(player2, new DaringFiendbonder());
        readyGraveyardAbility();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void exileCostIsPaidBeforeResolutionAndRemainsPaidWhenTargetDies() {
        Permanent target = addCreatureReady(player2, new DaringFiendbonder());
        readyGraveyardAbility();
        DaringFiendbonder source = (DaringFiendbonder) gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(source.getId()));
        harness.assertNotInGraveyard(player1, "Daring Fiendbonder");
        assertThat(target.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        target.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Daring Fiendbonder");
        assertThat(target.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(source.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void indestructibleCounterProtectsCreatureFromLethalDamage() {
        Permanent target = addCreatureReady(player2, new DaringFiendbonder());
        readyGraveyardAbility();
        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        target.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Daring Fiendbonder");
        harness.assertNotInGraveyard(player2, "Daring Fiendbonder");
    }

    @Test
    void cannotActivateDuringCombat() {
        Permanent target = addCreatureReady(player2, new DaringFiendbonder());
        readyGraveyardAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInGraveyard(player1, "Daring Fiendbonder");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        Permanent target = addCreatureReady(player2, new DaringFiendbonder());
        readyGraveyardAbility();
        harness.castFromHand(player1, new DaringFiendbonder(), "{3}{B}");

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertInGraveyard(player1, "Daring Fiendbonder");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotPayActivationCostWithoutBlackMana() {
        Permanent target = addCreatureReady(player2, new DaringFiendbonder());
        readyGraveyardAbility();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Daring Fiendbonder");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
    }

    private void readyGraveyardAbility() {
        harness.setGraveyard(player1, List.of(new DaringFiendbonder()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
