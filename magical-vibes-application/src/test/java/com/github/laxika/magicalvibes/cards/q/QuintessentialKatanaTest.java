package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HigureTheStillWind;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuintessentialKatana.class, GrizzlyBears.class, HigureTheStillWind.class})
class QuintessentialKatanaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        katana.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {2} attaches the Katana to a creature you control")
    void equipAttachesToCreature() {
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(katana.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature untaps and its controller gains 2 life after dealing combat damage")
    void combatDamageUntapsAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        katana.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.tap();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Accepting the Ninja trigger attaches the Katana to the entering Ninja")
    void attachesToEnteringNinja() {
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        harness.setHand(player1, List.of(new HigureTheStillWind()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent ninja = findPermanent(player1, "Higure, the Still Wind");
        assertThat(katana.getAttachedTo()).isEqualTo(ninja.getId());
    }

    @Test
    @DisplayName("The Ninja trigger does not fire for a non-Ninja creature")
    void doesNotAttachToNonNinja() {
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(katana.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The creature controller gains life when an opponent controls the Katana")
    void creatureControllerGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = harness.addToBattlefieldAndReturn(player2, new QuintessentialKatana());
        katana.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.tap();

        resolveCombat();
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Detaching the Katana does not stop the pending trigger from untapping its creature")
    void untapsOriginalCreatureAfterDetachment() {
        harness.setLife(player1, 10);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        katana.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.tap();

        resolveCombat();
        assertThat(gd.stack).isNotEmpty();
        katana.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Moving the Katana does not change which creature the pending trigger untaps")
    void movingKatanaDoesNotRedirectUntap() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        katana.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.tap();
        otherCreature.tap();

        resolveCombat();
        assertThat(gd.stack).isNotEmpty();
        katana.setAttachedTo(otherCreature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the Ninja trigger preserves the original attachment")
    void decliningNinjaAttachmentKeepsOriginalAttachment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        katana.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new HigureTheStillWind()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(katana.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An opponent's Ninja does not trigger attachment")
    void opponentNinjaDoesNotTriggerAttachment() {
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());

        harness.enterBattlefieldAndReturn(player2, new HigureTheStillWind());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(katana.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An entering Ninja can receive the Katana from its previous creature")
    void enteringNinjaReplacesExistingAttachment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        katana.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new HigureTheStillWind()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent ninja = findPermanent(player1, "Higure, the Still Wind");
        assertThat(katana.getAttachedTo()).isEqualTo(ninja.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature also untaps and gains life")
    void combatDamageToCreatureTriggersAbility() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuintessentialKatana());
        katana.setAttachedTo(creature.getId());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new QuintessentialKatana());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new QuintessentialKatana());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
