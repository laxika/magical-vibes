package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.e.EvilPresence;
import com.github.laxika.magicalvibes.cards.d.DroveOfElves;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlamerSpinners.class, GrizzlyBears.class, HolyStrength.class, Pacifism.class,
        EvilPresence.class, Island.class})
class GlamerSpinnersTest extends BaseCardTest {


    @Test
    @DisplayName("ETB trigger goes on the stack targeting the chosen permanent")
    void etbTriggerTargetsChosenPermanent() {
        Permanent enchanted = addCreature(player2);
        addAura(player2, new HolyStrength(), enchanted);
        addCreature(player2); // valid recipient

        castSpinners(enchanted.getId());
        harness.passBothPriorities(); // resolve the creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enchanted.getId());
    }

    @Test
    @DisplayName("Resolving prompts the controller to choose a same-controller recipient")
    void resolvingPromptsRecipientChoice() {
        Permanent enchanted = addCreature(player2);
        addAura(player2, new HolyStrength(), enchanted);
        Permanent recipient = addCreature(player2);

        resolveSpinnersEtb(enchanted.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).contains(recipient.getId()).doesNotContain(enchanted.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttachAllAurasToAnotherPermanent.class);
    }


    @Test
    @DisplayName("Choosing a recipient moves all Auras onto it")
    void movesAllAurasToChosenPermanent() {
        Permanent enchanted = addCreature(player2);
        Permanent holyStrength = addAura(player2, new HolyStrength(), enchanted);
        Permanent pacifism = addAura(player2, new Pacifism(), enchanted);
        Permanent recipient = addCreature(player2);

        resolveSpinnersEtb(enchanted.getId());
        harness.handlePermanentChosen(player1, recipient.getId());

        assertThat(holyStrength.getAttachedTo()).isEqualTo(recipient.getId());
        assertThat(pacifism.getAttachedTo()).isEqualTo(recipient.getId());
    }

    @Test
    @DisplayName("Moved Aura's static bonus applies to the new permanent, not the old one")
    void staticBonusTransfersToRecipient() {
        Permanent enchanted = addCreature(player2);
        addAura(player2, new HolyStrength(), enchanted); // +1/+2
        Permanent recipient = addCreature(player2); // Grizzly Bears 2/2

        resolveSpinnersEtb(enchanted.getId());
        harness.handlePermanentChosen(player1, recipient.getId());

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);
    }


    @Test
    @DisplayName("Only permanents controlled by the target's controller are valid recipients")
    void recipientMustShareTargetsController() {
        Permanent enchanted = addCreature(player2);
        addAura(player2, new HolyStrength(), enchanted);
        Permanent sameController = addCreature(player2);
        Permanent otherController = addCreature(player1); // Glamer Spinners' controller

        resolveSpinnersEtb(enchanted.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .contains(sameController.getId())
                .doesNotContain(otherController.getId());
    }


    @Test
    @DisplayName("A target with no Auras produces no choice and moves nothing")
    void targetWithNoAurasDoesNothing() {
        Permanent bare = addCreature(player2);
        addCreature(player2);

        resolveSpinnersEtb(bare.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("no Auras to move"));
    }

    @Test
    @DisplayName("When no permanent can receive the Auras, they stay attached")
    void noLegalRecipientAurasStay() {
        Permanent enchanted = addCreature(player2);
        Permanent aura = addAura(player2, new HolyStrength(), enchanted);
        addLand(player2, new Island()); // a land cannot receive a creature Aura

        resolveSpinnersEtb(enchanted.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(aura.getAttachedTo()).isEqualTo(enchanted.getId());
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("stay attached"));
    }


    @Test
    @DisplayName("A land Aura can only move to another land the same player controls")
    void landAuraOnlyMovesToLand() {
        Permanent enchantedLand = addLand(player2, new Island());
        addAura(player2, new EvilPresence(), enchantedLand);
        Permanent otherLand = addLand(player2, new Island());
        Permanent creature = addCreature(player2);

        resolveSpinnersEtb(enchantedLand.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .contains(otherLand.getId())
                .doesNotContain(creature.getId(), enchantedLand.getId());
    }

    @Test
    @DisplayName("Auras move regardless of their controller, without changing that controller")
    void movesAurasControlledByBothPlayers() {
        Permanent enchanted = addCreature(player2);
        Permanent friendlyAura = addAura(player1, new HolyStrength(), enchanted);
        Permanent opposingAura = addAura(player2, new Pacifism(), enchanted);
        Permanent recipient = addCreature(player2);

        resolveSpinnersEtb(enchanted.getId());
        harness.handlePermanentChosen(player1, recipient.getId());

        assertThat(friendlyAura.getAttachedTo()).isEqualTo(recipient.getId());
        assertThat(opposingAura.getAttachedTo()).isEqualTo(recipient.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(friendlyAura);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingAura);
    }

    @Test
    @DisplayName("Glamer Spinners itself can receive Auras from another permanent its controller controls")
    void spinnersCanReceiveAuras() {
        Permanent enchanted = addCreature(player1);
        Permanent aura = addAura(player2, new Pacifism(), enchanted);

        resolveSpinnersEtb(enchanted.getId());
        Permanent spinners = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof GlamerSpinners)
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, spinners.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(spinners.getId());
    }

    @Test
    @CardUsed({DroveOfElves.class})
    @DisplayName("An opponent's hexproof creature can receive Auras because the recipient is not targeted")
    void hexproofRecipientIsLegal() {
        Permanent enchanted = addCreature(player2);
        Permanent aura = addAura(player1, new HolyStrength(), enchanted);
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new DroveOfElves());

        resolveSpinnersEtb(enchanted.getId());
        harness.handlePermanentChosen(player1, recipient.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(recipient.getId());
    }

    @Test
    @DisplayName("Auras and recipients are determined when the trigger resolves")
    void usesBattlefieldAtResolution() {
        Permanent enchanted = addCreature(player2);
        castSpinners(enchanted.getId());
        harness.passBothPriorities();

        Permanent aura = addAura(player2, new HolyStrength(), enchanted);
        Permanent recipient = addCreature(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(recipient.getId());
    }

    private void castSpinners(UUID targetId) {
        harness.setHand(player1, List.of(new GlamerSpinners()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void resolveSpinnersEtb(UUID targetId) {
        castSpinners(targetId);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the ETB trigger
    }

    private Permanent addCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addLand(Player player, Card landCard) {
        return harness.addToBattlefieldAndReturn(player, landCard);
    }

    private Permanent addAura(Player owner, Card auraCard, Permanent target) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(owner, auraCard);
        auraPerm.setAttachedTo(target.getId());
        return auraPerm;
    }
}
