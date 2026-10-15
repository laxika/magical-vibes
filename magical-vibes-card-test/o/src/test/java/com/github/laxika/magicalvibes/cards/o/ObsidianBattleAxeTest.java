package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.c.CloakAndDagger;
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

@CardUsed({ObsidianBattleAxe.class, ElvishWarrior.class, GrizzlyBears.class, CloakAndDagger.class})
class ObsidianBattleAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1 and haste")
    void equippedCreatureGetsBoostAndHaste() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the bonuses when the Axe is removed")
    void creatureLosesBonusesWhenAxeRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(axe);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Resolving equip attaches the Axe to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Accepting the may attaches the Axe to the Warrior that entered")
    void attachesToEnteringWarriorOnAccept() {
        Permanent axe = addAxeReady(player1);

        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → Axe triggers, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent warrior = warriorOnBattlefield(player1);
        assertThat(axe.getAttachedTo()).isEqualTo(warrior.getId());
        // Elvish Warrior is a 2/3; the Axe's +2/+1 makes it a 4/4.
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the may leaves the Axe unattached")
    void staysUnattachedOnDecline() {
        Permanent axe = addAxeReady(player1);

        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(axe.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for a non-Warrior creature entering")
    void doesNotTriggerForNonWarrior() {
        addAxeReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell — no trigger for a Bear

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can attach to a Warrior an opponent controls")
    void attachesToOpponentWarrior() {
        Permanent axe = addAxeReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new ElvishWarrior()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // The Axe's controller (player1) makes the "you may" choice.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent warrior = warriorOnBattlefield(player2);
        assertThat(axe.getAttachedTo()).isEqualTo(warrior.getId());
    }

    @Test
    @DisplayName("Accepting the trigger moves the Axe and its bonuses to the entering Warrior")
    void movesFromPreviouslyEquippedCreature() {
        Permanent oldCreature = addCreatureReady(player1, new ElvishWarrior());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(oldCreature.getId());

        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent entering = findPermanents(player1, "Elvish Warrior").get(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(axe.getAttachedTo()).isEqualTo(entering.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oldCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, oldCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, entering, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the trigger preserves the existing attachment")
    void declinePreservesExistingAttachment() {
        Permanent oldCreature = addCreatureReady(player1, new ElvishWarrior());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(oldCreature.getId());

        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(axe.getAttachedTo()).isEqualTo(oldCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, oldCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed(CloakAndDagger.class)
    @DisplayName("The attachment trigger can attach to an entering Warrior with shroud")
    void triggerDoesNotTargetEnteringWarrior() {
        Permanent axe = addAxeReady(player1);
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakAndDagger());

        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent warrior = warriorOnBattlefield(player1);
        cloak.setAttachedTo(warrior.getId());
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.SHROUD)).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(axe.getAttachedTo()).isEqualTo(warrior.getId());
    }

    private Permanent addAxeReady(Player player) {
        return addCreatureReady(player, new ObsidianBattleAxe());
    }

    private Permanent warriorOnBattlefield(Player player) {
        return findPermanent(player, "Elvish Warrior");
    }
}
