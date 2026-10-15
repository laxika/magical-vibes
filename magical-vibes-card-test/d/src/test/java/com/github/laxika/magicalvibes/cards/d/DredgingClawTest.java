package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.w.WhispersilkCloak;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DredgingClaw.class, ReassemblingSkeleton.class, WhispersilkCloak.class})
class DredgingClawTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and menace")
    void equippedCreatureGetsBoostAndMenace() {
        Permanent creature = addCreatureReady(player1, new ReassemblingSkeleton());
        Permanent claw = addClawReady(player1);
        claw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Resolving equip attaches Dredging Claw to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent claw = addClawReady(player1);
        Permanent creature = addCreatureReady(player1, new ReassemblingSkeleton());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(claw.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("May attach to a creature entering from your graveyard")
    void mayAttachToCreatureEnteringFromGraveyard() {
        Permanent claw = addClawReady(player1);
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent skeleton = findPermanent(player1, "Reassembling Skeleton");
        assertThat(claw.getAttachedTo()).isEqualTo(skeleton.getId());
    }

    @Test
    void decliningAttachmentKeepsPreviousCreatureEquipped() {
        Permanent oldCreature = addCreatureReady(player1, new ReassemblingSkeleton());
        Permanent claw = addClawReady(player1);
        claw.setAttachedTo(oldCreature.getId());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(claw.getAttachedTo()).isEqualTo(oldCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
    }

    @Test
    void acceptingAttachmentMovesBoostAndMenaceToEnteringCreature() {
        Permanent oldCreature = addCreatureReady(player1, new ReassemblingSkeleton());
        Permanent claw = addClawReady(player1);
        claw.setAttachedTo(oldCreature.getId());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returnedCreature = findPermanents(player1, "Reassembling Skeleton").stream()
                .filter(p -> !p.getId().equals(oldCreature.getId())).findFirst().orElseThrow();
        assertThat(claw.getAttachedTo()).isEqualTo(returnedCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, oldCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, returnedCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, returnedCreature, Keyword.MENACE)).isTrue();
    }

    @Test
    void creatureEnteringFromOpponentsGraveyardDoesNotTrigger() {
        Permanent claw = addClawReady(player1);
        harness.setGraveyard(player2, List.of(new ReassemblingSkeleton()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Reassembling Skeleton")).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(claw.getAttachedTo()).isNull();
    }

    @Test
    void attachmentTriggerDoesNotTargetEnteringCreatureWithShroud() {
        Permanent claw = addClawReady(player1);
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new WhispersilkCloak());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent returnedCreature = findPermanent(player1, "Reassembling Skeleton");
        cloak.setAttachedTo(returnedCreature.getId());
        assertThat(gqs.hasKeyword(gd, returnedCreature, Keyword.SHROUD)).isTrue();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(claw.getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    private Permanent addClawReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DredgingClaw());
    }
}
