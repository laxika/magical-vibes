package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SageEyeAvengersTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess pumps Sage-Eye Avengers for a noncreature spell")
    void prowessPumpsForNoncreatureSpell() {
        Permanent avengers = addReadyAvengers();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avengers)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, avengers)).isEqualTo(6);
    }

    @Test
    @DisplayName("Attack trigger returns a target creature with less power")
    void attackTriggerReturnsCreatureWithLessPower() {
        addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger does not return an equal-power creature")
    void attackTriggerDoesNotReturnEqualPowerCreature() {
        addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger uses the source's last known power if it leaves")
    void attackTriggerUsesLastKnownSourcePower() {
        Permanent avengers = addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(avengers);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private Permanent addReadyAvengers() {
        Permanent avengers = harness.addToBattlefieldAndReturn(player1, new SageEyeAvengers());
        avengers.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return avengers;
    }
}
