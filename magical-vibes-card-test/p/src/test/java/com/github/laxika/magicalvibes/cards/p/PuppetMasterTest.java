package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PuppetMaster.class, DurkwoodBoars.class})
class PuppetMasterTest extends BaseCardTest {

    private void attachPuppetMaster(Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PuppetMaster());
        aura.setAttachedTo(enchanted.getId());
    }

    @Test
    @DisplayName("Returns the enchanted creature, then paying {U}{U}{U} returns Puppet Master")
    void paysToReturnAuraAfterCreatureReturns() {
        Permanent boars = harness.addToBattlefieldAndReturn(player1, new DurkwoodBoars());
        attachPuppetMaster(boars);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, boars));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Durkwood Boars");
        harness.assertInHand(player1, "Puppet Master");
        harness.assertNotInGraveyard(player1, "Puppet Master");
    }

    @Test
    @DisplayName("Declining the payment leaves Puppet Master in its owner's graveyard")
    void decliningPaymentLeavesAuraInGraveyard() {
        Permanent boars = harness.addToBattlefieldAndReturn(player1, new DurkwoodBoars());
        attachPuppetMaster(boars);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, boars));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Durkwood Boars");
        harness.assertInGraveyard(player1, "Puppet Master");
        harness.assertNotInHand(player1, "Puppet Master");
    }

    @Test
    @DisplayName("Accepting the payment without enough mana leaves Puppet Master in its owner's graveyard")
    void cannotPayFollowUpWithoutEnoughMana() {
        Permanent boars = harness.addToBattlefieldAndReturn(player1, new DurkwoodBoars());
        attachPuppetMaster(boars);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, boars));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Durkwood Boars");
        harness.assertInGraveyard(player1, "Puppet Master");
        harness.assertNotInHand(player1, "Puppet Master");
    }

    @Test
    void returnsOpponentsCreatureToItsOwnerButAuraControllerPays() {
        Permanent boars = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        attachPuppetMaster(boars);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, boars));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Durkwood Boars");
        harness.assertNotInHand(player1, "Durkwood Boars");
        harness.assertInHand(player1, "Puppet Master");
    }

    @Test
    void doesNotReturnCreatureThatLeftGraveyardAndDiedAgain() {
        DurkwoodBoars card = new DurkwoodBoars();
        Permanent boars = harness.addToBattlefieldAndReturn(player1, card);
        attachPuppetMaster(boars);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, boars));
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId());
            harness.getPermanentRemovalService().addCardToHandFromGraveyard(
                    gd, player1.getId(), player1.getId(), card);
        });
        gd.playerHands.get(player1.getId()).remove(card);
        Permanent returnedBoars = harness.addToBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returnedBoars));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Durkwood Boars");
        harness.assertNotInHand(player1, "Durkwood Boars");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void doesNotOfferPaymentWhenCreatureHasLeftGraveyard() {
        DurkwoodBoars card = new DurkwoodBoars();
        Permanent boars = harness.addToBattlefieldAndReturn(player1, card);
        attachPuppetMaster(boars);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, boars));
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId());
            harness.getPermanentRemovalService().addCardToHandFromGraveyard(
                    gd, player1.getId(), player1.getId(), card);
        });
        harness.passBothPriorities();

        harness.assertInHand(player1, "Durkwood Boars");
        harness.assertInGraveyard(player1, "Puppet Master");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void doesNotReturnAuraThatLeftGraveyardAndEnteredItAgain() {
        Permanent boars = harness.addToBattlefieldAndReturn(player1, new DurkwoodBoars());
        PuppetMaster card = new PuppetMaster();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, card);
        aura.setAttachedTo(boars.getId());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, boars));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removeOrphanedAuras(gd));
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId());
            harness.getPermanentRemovalService().addCardToHandFromGraveyard(
                    gd, player1.getId(), player1.getId(), card);
        });
        gd.playerHands.get(player1.getId()).remove(card);
        Permanent otherBoars = harness.addToBattlefieldAndReturn(player1, new DurkwoodBoars());
        Permanent returnedAura = harness.addToBattlefieldAndReturn(player1, card);
        returnedAura.setAttachedTo(otherBoars.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returnedAura));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Durkwood Boars");
        harness.assertInGraveyard(player1, "Puppet Master");
        harness.assertNotInHand(player1, "Puppet Master");
    }
}
