package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.h.HorseshoeCrab;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryMantle.class, HorseshoeCrab.class, WornPowerstone.class, Duress.class})
class FieryMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fiery Mantle attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HorseshoeCrab());
        harness.setHand(player1, List.of(new FieryMantle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FieryMantle
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Activating Fiery Mantle boosts the enchanted creature")
    void activatingAbilityBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new HorseshoeCrab());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FieryMantle());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fiery Mantle's activated boost expires at end of turn")
    void activatedBoostExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new HorseshoeCrab());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FieryMantle());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fiery Mantle returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HorseshoeCrab());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FieryMantle());
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fiery Mantle");
        harness.assertNotInGraveyard(player1, "Fiery Mantle");
        harness.assertNotOnBattlefield(player1, "Fiery Mantle");
    }

    @Test
    @DisplayName("Fiery Mantle returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HorseshoeCrab());
        FieryMantle mantleCard = new FieryMantle();
        mantleCard.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, mantleCard);
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fiery Mantle");
        harness.assertNotInHand(player2, "Fiery Mantle");
        harness.assertNotInGraveyard(player1, "Fiery Mantle");
    }

    @Test
    @DisplayName("An activated boost still resolves after Fiery Mantle leaves the battlefield")
    void activatedBoostResolvesAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HorseshoeCrab());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FieryMantle());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        resolveAllTriggers();

        harness.assertInHand(player1, "Fiery Mantle");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fiery Mantle returns after its enchanted creature dies")
    void returnsAfterEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HorseshoeCrab());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FieryMantle());
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Horseshoe Crab");
        harness.assertInHand(player1, "Fiery Mantle");
        harness.assertNotInGraveyard(player1, "Fiery Mantle");
        harness.assertNotOnBattlefield(player1, "Fiery Mantle");
    }

    @Test
    @DisplayName("A stolen Mantle's controller controls its trigger and only that Mantle returns")
    void lastControllerControlsTriggerReturningOnlySourceCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HorseshoeCrab());
        FieryMantle otherMantle = new FieryMantle();
        harness.setGraveyard(player1, List.of(otherMantle));
        FieryMantle mantle = new FieryMantle();
        mantle.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, mantle);
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(mantle).doesNotContain(otherMantle);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherMantle).doesNotContain(mantle);
        harness.assertNotInHand(player2, "Fiery Mantle");
    }

    @Test
    @DisplayName("Fiery Mantle cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.setHand(player1, List.of(new FieryMantle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fiery Mantle does not return when put into a graveyard from hand")
    void doesNotReturnWhenPutIntoGraveyardFromHand() {
        harness.setHand(player1, List.of(new FieryMantle()));
        harness.setHand(player2, List.of(new Duress()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Fiery Mantle");
        harness.assertNotInHand(player1, "Fiery Mantle");
        assertThat(gd.stack).isEmpty();
    }
}
