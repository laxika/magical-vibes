package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageEyeAvengers.class, GrizzlyBears.class, Shock.class, Cloudshift.class})
class SageEyeAvengersTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess pumps Sage-Eye Avengers for a noncreature spell")
    void prowessPumpsForNoncreatureSpell() {
        Permanent avengers = addReadyAvengers();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avengers)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, avengers)).isEqualTo(6);
    }

    @Test
    @DisplayName("Prowess does not trigger for creature spells")
    void prowessDoesNotTriggerForCreatureSpell() {
        Permanent avengers = addReadyAvengers();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avengers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, avengers)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentsSpellDoesNotTriggerProwess() {
        Permanent avengers = addReadyAvengers();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, avengers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, avengers)).isEqualTo(5);
    }

    @Test
    @DisplayName("Prowess bonuses stack and expire at end of turn")
    void prowessBonusesStackAndExpire() {
        Permanent avengers = addReadyAvengers();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avengers)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, avengers)).isEqualTo(7);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, avengers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, avengers)).isEqualTo(5);
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

    @Test
    @DisplayName("The controller may decline to return the target creature")
    void attackTriggerMayBeDeclined() {
        addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger can return a creature controlled by its controller")
    void attackTriggerCanReturnOwnCreature() {
        addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A target initially too large can be returned after prowess increases the source's power")
    void attackTriggerUsesSourcePowerAtResolution() {
        Permanent avengers = addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setPowerModifier(2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, avengers)).isEqualTo(5);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger does nothing if the target's power grows to equal the source's power")
    void attackTriggerUsesTargetPowerAtResolution() {
        addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        target.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger fizzles if its target is destroyed in response")
    void attackTriggerFizzlesWhenTargetLeaves() {
        addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger uses the original source's last known power after it leaves and returns")
    void attackTriggerUsesLastKnownPowerAfterSourceFlickers() {
        Permanent avengers = addReadyAvengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SageEyeAvengers());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, avengers.getId());
        assertThat(gqs.getEffectivePower(gd, avengers)).isEqualTo(5);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Sage-Eye Avengers");
        assertThat(returned.getId()).isNotEqualTo(avengers.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotOnBattlefield(player2, "Sage-Eye Avengers");
        harness.assertInHand(player2, "Sage-Eye Avengers");
    }

    private Permanent addReadyAvengers() {
        Permanent avengers = addCreatureReady(player1, new SageEyeAvengers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return avengers;
    }
}
