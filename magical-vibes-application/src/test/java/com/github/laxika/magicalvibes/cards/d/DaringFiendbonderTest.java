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
        harness.setGraveyard(player1, List.of(new DaringFiendbonder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyGraveyardAbility() {
        harness.setGraveyard(player1, List.of(new DaringFiendbonder()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
