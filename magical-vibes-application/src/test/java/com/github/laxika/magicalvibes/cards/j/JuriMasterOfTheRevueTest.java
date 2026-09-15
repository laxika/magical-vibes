package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuriMasterOfTheRevue.class, DiabolicEdict.class, FlameJavelin.class,
        GrizzlyBears.class, LlanowarElves.class})
class JuriMasterOfTheRevueTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you sacrifice a permanent, Juri gets a +1/+1 counter")
    void controllerSacrificeAddsCounter() {
        Permanent juri = harness.addToBattlefieldAndReturn(player1, new JuriMasterOfTheRevue());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEdictAt(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(juri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Juri does not trigger when an opponent sacrifices a permanent")
    void opponentSacrificeDoesNotAddCounter() {
        Permanent juri = harness.addToBattlefieldAndReturn(player1, new JuriMasterOfTheRevue());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(juri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("When Juri dies, it deals damage equal to its power to a player")
    void deathTriggerDealsPowerDamageToPlayer() {
        Permanent juri = harness.addToBattlefieldAndReturn(player1, new JuriMasterOfTheRevue());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrificeOwnPermanent(bears);
        assertThat(juri.getEffectivePower()).isEqualTo(2);

        int lifeBefore = gd.getLife(player2.getId());
        killJuriWithFlameJavelin(juri.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("When Juri dies, its death trigger can target a creature")
    void deathTriggerDealsDamageToCreature() {
        Permanent juri = harness.addToBattlefieldAndReturn(player1, new JuriMasterOfTheRevue());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        killJuriWithFlameJavelin(juri.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(elvesId, player2.getId());
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    private void sacrificeOwnPermanent(Permanent permanent) {
        castEdictAt(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, permanent.getId());
        harness.passBothPriorities();
    }

    private void castEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
    }

    private void killJuriWithFlameJavelin(UUID juriId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castInstant(player2, 0, juriId);
        harness.passBothPriorities();
    }
}
