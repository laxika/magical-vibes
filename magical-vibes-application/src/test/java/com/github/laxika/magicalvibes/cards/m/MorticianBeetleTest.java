package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MorticianBeetleTest extends BaseCardTest {

    @Test
    @DisplayName("When another player sacrifices a creature, Mortician Beetle may get a counter")
    void opponentSacrificeAddsCounter() {
        addCreatureReady(player1, new MorticianBeetle());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mortician Beetle's controller's creature sacrifice also triggers it")
    void controllerSacrificeAddsCounter() {
        addCreatureReady(player1, new MorticianBeetle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEdictAt(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining Mortician Beetle's trigger leaves it without a counter")
    void declineLeavesBeetleUnchanged() {
        addCreatureReady(player1, new MorticianBeetle());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent morticianBeetle() {
        return findPermanent(player1, "Mortician Beetle");
    }

    private void castEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, target.getId());
    }
}
