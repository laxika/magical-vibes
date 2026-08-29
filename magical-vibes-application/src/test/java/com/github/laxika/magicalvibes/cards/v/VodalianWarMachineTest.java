package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GoblinChirurgeon;
import com.github.laxika.magicalvibes.cards.g.GoblinGrenade;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
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

@CardUsed({VodalianWarMachine.class, RiverMerfolk.class, GoblinChirurgeon.class, GoblinGrenade.class})
class VodalianWarMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Merfolk lets it attack despite defender")
    void tappingMerfolkLetsItAttack() {
        Permanent machine = addCreatureReady(player1, new VodalianWarMachine());
        addCreatureReady(player1, new RiverMerfolk());
        addCreatureReady(player2, new RiverMerfolk());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(machine.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Tapping a Merfolk gives it +2/+1 until end of turn")
    void tappingMerfolkBoostsIt() {
        Permanent machine = harness.addToBattlefieldAndReturn(player1, new VodalianWarMachine());
        harness.addToBattlefield(player1, new RiverMerfolk());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, machine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, machine)).isEqualTo(5);
    }

    @Test
    @DisplayName("Its death trigger destroys only Merfolk tapped to pay for its abilities")
    void deathTriggerDestroysTrackedMerfolk() {
        Permanent machine = harness.addToBattlefieldAndReturn(player1, new VodalianWarMachine());
        Permanent paidForAttack = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());
        Permanent paidForBoost = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());
        Permanent unrelatedMerfolk = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());
        Permanent goblin = addCreatureReady(player2, new GoblinChirurgeon());
        unrelatedMerfolk.tap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, paidForAttack.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GoblinGrenade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorceryWithSacrifice(player2, 0, machine.getId(), goblin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(paidForAttack, paidForBoost)
                .contains(unrelatedMerfolk);
        harness.assertInGraveyard(player1, "River Merfolk");
    }

    @Test
    @DisplayName("Its abilities require an untapped Merfolk")
    void cannotActivateWithoutUntappedMerfolk() {
        addCreatureReady(player1, new VodalianWarMachine());
        Permanent merfolk = addCreatureReady(player1, new RiverMerfolk());
        merfolk.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
