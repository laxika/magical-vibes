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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DivinersWandTest extends BaseCardTest {

    // ===== Granted draw trigger: +1/+1 and flying per card drawn =====

    @Test
    @DisplayName("Equipped creature gets +1/+1 and flying whenever its controller draws")
    void drawTriggerBoostsAndGrantsFlying() {
        Permanent creature = addReadyCreature(player1);
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
        harness.passBothPriorities(); // resolve first wand trigger
        harness.passBothPriorities(); // resolve second wand trigger

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addReadyCreature(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Draw trigger does not boost anything while the Wand is unattached")
    void unattachedDrawTriggerDoesNothing() {
        Permanent creature = addReadyCreature(player1);
        addWandReady(player1); // left unattached

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    // ===== Granted activated ability: {4}: Draw a card =====

    @Test
    @DisplayName("Equipped creature can pay {4} to draw a card")
    void grantedActivatedAbilityDraws() {
        Permanent creature = addReadyCreature(player1);
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

    // ===== Trigger: Wizard creature enters =====

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

    // ===== Helpers =====

    private Permanent addWandReady(Player player) {
        Permanent perm = new Permanent(new DivinersWand());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent wizardOnBattlefield(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();
    }
}
