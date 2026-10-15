package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormriderRig.class, ColossodonYearling.class, PincherBeetles.class})
class StormriderRigTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new StormriderRig());
        rig.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip {2} attaches Stormrider Rig to a creature you control")
    void equipAttachesToCreature() {
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new StormriderRig());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(rig.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Whenever a creature you control enters, Stormrider Rig may attach to it")
    void attachesToEnteringCreatureWhenAccepted() {
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new StormriderRig());

        harness.setHand(player1, List.of(new ColossodonYearling()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent creature = findPermanent(player1, "Colossodon Yearling");
        assertThat(rig.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining Stormrider Rig's may ability leaves it unattached")
    void staysUnattachedWhenDeclined() {
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new StormriderRig());

        harness.setHand(player1, List.of(new ColossodonYearling()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(rig.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Accepting the trigger moves the Rig from its previous creature")
    void movesFromPreviouslyEquippedCreature() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new StormriderRig());
        rig.setAttachedTo(original.getId());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new ColossodonYearling());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(rig.getAttachedTo()).isEqualTo(entering.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the trigger preserves the previous attachment")
    void decliningPreservesPreviousAttachment() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new StormriderRig());
        rig.setAttachedTo(original.getId());
        harness.enterBattlefieldAndReturn(player1, new ColossodonYearling());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(rig.getAttachedTo()).isEqualTo(original.getId());
    }

    @Test
    @DisplayName("An opponent's entering creature does not trigger the Rig")
    void opponentCreatureDoesNotTrigger() {
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new StormriderRig());

        harness.enterBattlefieldAndReturn(player2, new ColossodonYearling());

        assertThat(gd.stack).isEmpty();
        assertThat(rig.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("A noncreature entering does not trigger the Rig")
    void noncreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new StormriderRig());

        harness.enterBattlefieldAndReturn(player1, new StormriderRig());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An old trigger cannot attach a Rig that left and returned")
    void returningRigIsNotAttachedByOldTrigger() {
        StormriderRig card = new StormriderRig();
        Permanent rig = harness.addToBattlefieldAndReturn(player1, card);
        harness.enterBattlefieldAndReturn(player1, new ColossodonYearling());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, rig));
        harness.setExile(player1, List.of());
        Permanent returnedRig = harness.enterBattlefieldAndReturn(player1, card);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(returnedRig.getAttachedTo()).isNull();
    }
    @Test
    @DisplayName("The entering-creature trigger can attach to a creature with shroud")
    void triggerDoesNotTargetEnteringCreature() {
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new StormriderRig());
        harness.setHand(player1, List.of(new PincherBeetles()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent creature = findPermanent(player1, "Pincher Beetles");
        assertThat(rig.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
