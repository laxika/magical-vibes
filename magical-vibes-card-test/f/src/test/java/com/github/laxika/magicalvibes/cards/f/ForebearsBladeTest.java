package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForebearsBlade.class, GrizzlyBears.class, Deathmark.class})
class ForebearsBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);   // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has trample")
    void equippedCreatureHasTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature does not get boost or keywords")
    void unequippedCreatureNoBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses boost and keywords when blade is removed")
    void creatureLosesEffectsWhenBladeRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(blade);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("When equipped creature dies, blade attaches to target creature you control")
    void deathTriggerAttachesToAnotherCreature() {
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(creature1.getId());

        // Opponent destroys the equipped creature with Deathmark
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, creature1.getId());

        // Player chooses creature2 as the target for the blade's death trigger
        harness.handlePermanentChosen(player1, creature2.getId());
        harness.passBothPriorities(); // resolve the attach trigger

        // Blade should now be attached to creature2
        assertThat(blade.getAttachedTo()).isEqualTo(creature2.getId());
        // creature2 should now benefit from the blade's boost
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Death trigger has no valid targets when no other creatures exist — blade stays unattached")
    void deathTriggerNoValidTargets() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(creature.getId());

        // Opponent destroys the only creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, creature.getId());

        // Blade should still be on the battlefield but unattached
        harness.assertOnBattlefield(player1, "Forebear's Blade");
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Death trigger does not fire for a different creature dying")
    void triggerDoesNotFireForDifferentCreature() {
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(creature1.getId());

        // Opponent destroys the NON-equipped creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, creature2.getId());

        // Blade should still be attached to creature1
        assertThat(blade.getAttachedTo()).isEqualTo(creature1.getId());
    }

    @Test
    @DisplayName("Blade stays on battlefield after equipped creature dies")
    void bladeStaysOnBattlefieldAfterCreatureDies() {
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(creature1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, creature1.getId());

        // Choose creature2 as target
        harness.handlePermanentChosen(player1, creature2.getId());
        harness.passBothPriorities(); // resolve attach trigger

        // Creature1 should be in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Blade should still be on the battlefield
        harness.assertOnBattlefield(player1, "Forebear's Blade");
    }

    @Test
    @DisplayName("Resolving equip ability attaches blade to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Blade can be moved to another creature via equip")
    void canReEquipToAnotherCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

        blade.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(5);

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blade can be cast with no creatures on the battlefield")
    void canCastWithoutCreatures() {
        harness.setHand(player1, List.of(new ForebearsBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forebear's Blade");
        assertThat(findPermanent(player1, "Forebear's Blade").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip pays three generic mana without tapping the Equipment")
    void equipPaysThreeManaWithoutTapping() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(blade.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new ForebearsBlade());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void cannotEquipNoncreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blade.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Equip is restricted to sorcery speed")
    void cannotEquipDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ForebearsBlade());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Death trigger offers only creatures controlled by the Equipment's controller")
    void deathTriggerExcludesOpponentsCreatures() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(equipped.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, equipped.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(ownTarget.getId());
        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.passBothPriorities();
        assertThat(blade.getAttachedTo()).isEqualTo(ownTarget.getId());
    }

    @Test
    @DisplayName("Death trigger has no legal target when only an opponent's creature remains")
    void deathTriggerCannotUseOnlyOpponentsCreature() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ForebearsBlade());
        blade.setAttachedTo(equipped.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, equipped.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }
}
