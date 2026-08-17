package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DrownerOfSecrets;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VodalianWarMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Merfolk lets it attack despite defender")
    void tappingMerfolkLetsItAttack() {
        Permanent machine = harness.addToBattlefieldAndReturn(player1, new VodalianWarMachine());
        machine.setSummoningSick(false);
        harness.addToBattlefield(player1, new DrownerOfSecrets());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(machine.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Tapping a Merfolk gives it +2/+1 until end of turn")
    void tappingMerfolkBoostsIt() {
        Permanent machine = harness.addToBattlefieldAndReturn(player1, new VodalianWarMachine());
        harness.addToBattlefield(player1, new DrownerOfSecrets());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, machine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, machine)).isEqualTo(5);
    }

    @Test
    @DisplayName("Its death trigger destroys only Merfolk tapped to pay for its abilities")
    void deathTriggerDestroysTrackedMerfolk() {
        Permanent machine = harness.addToBattlefieldAndReturn(player1, new VodalianWarMachine());
        Permanent paidMerfolk = harness.addToBattlefieldAndReturn(player1, new DrownerOfSecrets());
        Permanent unrelatedMerfolk = harness.addToBattlefieldAndReturn(player1, new DrownerOfSecrets());
        harness.addToBattlefield(player1, new GrizzlyBears());
        unrelatedMerfolk.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, machine.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(paidMerfolk)
                .contains(unrelatedMerfolk);
        harness.assertInGraveyard(player1, "Drowner of Secrets");
    }
}
