package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaskOfMemory.class, YotianSoldier.class, Forest.class, MarchOfTheMachines.class})
class MaskOfMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Mask of Memory attaches it to a creature")
    void equippingAttachesToCreature() {
        Permanent mask = addMaskReady(player1);
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Combat damage with Mask of Memory draws two cards and discards one when accepted")
    void acceptedCombatDamageTriggerLoots() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining Mask of Memory's trigger does not draw or discard")
    void decliningCombatDamageTriggerDoesNothing() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Mask of Memory does not trigger when equipped creature deals combat damage only to a creature")
    void combatDamageToCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new YotianSoldier());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An unequipped Mask of Memory does not trigger from combat damage")
    void unequippedMaskDoesNotTrigger() {
        addMaskReady(player1);
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        creature.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The Mask's controller draws and discards even when the opponent controls the equipped creature")
    void maskControllerLootsWhenOpponentControlsCreature() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent mask = addMaskReady(player2);
        mask.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing Mask of Memory after it triggers does not prevent drawing and discarding")
    void triggerResolvesAfterMaskLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(mask);
        gd.playerGraveyards.get(player1.getId()).add(mask.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An animated Mask of Memory does not trigger from its own combat damage")
    void animatedMaskDoesNotTriggerFromItsOwnDamage() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent mask = addMaskReady(player1);
        mask.setAttacking(true);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addMaskReady(Player player) {
        return addCreatureReady(player, new MaskOfMemory());
    }
}
