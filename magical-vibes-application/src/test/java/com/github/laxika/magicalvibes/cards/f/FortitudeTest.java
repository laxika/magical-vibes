package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fortitude.class, Forest.class, GorillaWarrior.class})
class FortitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fortitude attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        harness.setHand(player1, List.of(new Fortitude()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Fortitude");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Sacrificing a Forest grants a shield that saves the enchanted creature from lethal damage")
    void sacrificingForestRegeneratesEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GorillaWarrior());
        Permanent forestToSacrifice = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent remainingForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Fortitude());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestToSacrifice.getId());
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remainingForest);
        harness.assertInGraveyard(player1, "Forest");

        creature.setMarkedDamage(gqs.getEffectiveToughness(gd, creature));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fortitude can enchant a creature an opponent controls")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        harness.setHand(player1, List.of(new Fortitude()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Fortitude");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Fortitude cannot be activated without a Forest to sacrifice")
    void cannotActivateWithoutForest() {
        Permanent creature = addCreatureReady(player1, new GorillaWarrior());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Fortitude());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fortitude returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Fortitude());
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fortitude");
        harness.assertNotInGraveyard(player1, "Fortitude");
        harness.assertNotOnBattlefield(player1, "Fortitude");
    }

    @Test
    @DisplayName("A stolen Fortitude's last controller controls its return ability")
    void stolenAuraReturnsToOwnerWithTriggerControlledByLastController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        Fortitude card = new Fortitude();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        harness.assertInGraveyard(player1, "Fortitude");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fortitude");
        harness.assertNotInHand(player2, "Fortitude");
        harness.assertNotInGraveyard(player1, "Fortitude");
    }

    @Test
    @DisplayName("Regeneration still resolves after Fortitude leaves the battlefield")
    void regenerationResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Fortitude());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fortitude");
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        creature.setMarkedDamage(gqs.getEffectiveToughness(gd, creature));
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fortitude returns when its enchanted creature dies and the unattached Aura goes to the graveyard")
    void returnsAfterEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Fortitude());
        aura.setAttachedTo(creature.getId());

        creature.setMarkedDamage(gqs.getEffectiveToughness(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gorilla Warrior");
        harness.assertInHand(player1, "Fortitude");
        harness.assertNotInGraveyard(player1, "Fortitude");
        harness.assertNotOnBattlefield(player1, "Fortitude");
    }

    @Test
    @DisplayName("Fortitude cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Fortitude()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
