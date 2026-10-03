package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivinersWand.class, CounselOfTheSoratami.class, FugitiveWizard.class, GrizzlyBears.class})
class DivinersWandTest extends BaseCardTest {


    @Test
    @DisplayName("Equipped creature gets +1/+1 and flying whenever its controller draws")
    void drawTriggerBoostsAndGrantsFlying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Counsel of the Soratami draws 2 cards → two draw triggers.
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Counsel (draws 2)
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Draw trigger does not fire at all while the Wand is unattached")
    void unattachedDrawTriggerDoesNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addWandReady(player1); // left unattached

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Counsel (draws 2)

        // An unattached Equipment grants the draw ability to no creature — nothing triggers.
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }


    @Test
    @DisplayName("Equipped creature can pay {4} to draw a card")
    void grantedActivatedAbilityDraws() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());
        gd.playerHands.get(player1.getId()).clear();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null); // creature is index 0
        harness.passBothPriorities(); // resolve draw
        harness.passBothPriorities(); // resolve the draw trigger it caused

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // The card it drew triggered the Wand's own draw ability.
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }


    @Test
    @DisplayName("Accepting the may attaches the Wand to the Wizard that entered")
    void attachesToEnteringWizardOnAccept() {
        Permanent wand = addWandReady(player1);

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature → wand triggers
        harness.passBothPriorities(); // resolve may-ability → prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent wizard = wizardOnBattlefield(player1);
        assertThat(wand.getAttachedTo()).isEqualTo(wizard.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Wizard creature entering")
    void doesNotTriggerForNonWizard() {
        addWandReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature — no trigger for a Bear

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }


    @Test
    @DisplayName("The equipped creature's controller draws to trigger the bonus")
    void creatureControllerDrawTriggersAcrossControllers() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        castDrawTwo(player2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Equipment controller's draws do not trigger an opponent's creature")
    void equipmentControllerDrawDoesNotTriggerOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        castDrawTwo(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Pending draw triggers still boost the original creature after the Wand moves")
    void pendingDrawTriggersRememberOriginalCreature() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent next = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(original.getId());

        castDrawTwo(player1);
        wand.setAttachedTo(next.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, next)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, next, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Pending draw triggers survive the Wand leaving the battlefield")
    void pendingDrawTriggersSurviveWandRemoval() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        castDrawTwo(player1);
        gd.playerBattlefields.get(player1.getId()).remove(wand);
        gd.playerGraveyards.get(player1.getId()).add(wand.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining attachment leaves the Wand on its current creature")
    void decliningAttachmentKeepsCurrentCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        harness.enterBattlefieldAndReturn(player1, new FugitiveWizard());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(wand.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The Wand can attach to an opponent's entering Wizard")
    void attachesToOpponentsWizard() {
        Permanent wand = addWandReady(player1);
        Permanent wizard = harness.enterBattlefieldAndReturn(player2, new FugitiveWizard());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wand.getAttachedTo()).isEqualTo(wizard.getId());
    }

    @Test
    @DisplayName("Equip attaches the Wand for three mana")
    void equipAttachesForThreeMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, creature.getId());
        resolveAllTriggers();

        assertThat(wand.getAttachedTo()).isEqualTo(creature.getId());
    }

    private void castDrawTwo(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new CounselOfTheSoratami()));
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.castSorcery(player, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addWandReady(Player player) {
        return addCreatureReady(player, new DivinersWand());
    }

    private Permanent wizardOnBattlefield(Player player) {
        return findPermanent(player, "Fugitive Wizard");
    }
}
