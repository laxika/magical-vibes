package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeavyArbalestTest extends BaseCardTest {

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Heavy Arbalest to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent arbalest = addArbalestReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(arbalest.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Granted activated ability: deal 2 damage to creature =====

    @Test
    @DisplayName("Equipped creature can tap to deal 2 damage to target creature")
    void grantedAbilityDeals2DamageToCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        Permanent targetCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        // Grizzly Bears has 2 toughness, 2 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(targetCreature.getId()));
        // The equipped creature should be tapped
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Granted activated ability: deal 2 damage to player =====

    @Test
    @DisplayName("Equipped creature can tap to deal 2 damage to a player")
    void grantedAbilityDeals2DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = new Permanent(new GrizzlyBears());
        // summoningSick defaults to true
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Already tapped =====

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();

        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Doesn't untap during controller's untap step =====

    @Test
    @DisplayName("Equipped creature does not untap during controller's untap step")
    void equippedCreatureDoesNotUntap() {
        Permanent creature = addReadyCreature(player2);
        creature.tap();

        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(creature.getId());

        // Advance to player2's turn to trigger untap
        advanceToNextTurn(player1);

        // The creature should still be tapped
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped equipped creature remains untapped (doesn't tap it)")
    void untappedEquippedCreatureRemainsUntapped() {
        Permanent creature = addReadyCreature(player2);

        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(creature.getId());

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Creature was untapped and stays untapped (effect only prevents untapping)
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other permanents still untap normally when one creature has Heavy Arbalest")
    void otherPermanentsStillUntap() {
        Permanent equippedCreature = addReadyCreature(player2);
        equippedCreature.tap();

        Permanent freeCreature = addReadyCreature(player2);
        freeCreature.tap();

        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(equippedCreature.getId());

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Equipped creature stays tapped, free creature untaps
        assertThat(equippedCreature.isTapped()).isTrue();
        assertThat(freeCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature can untap again after Heavy Arbalest is removed")
    void creatureUntapsAfterArbalestRemoved() {
        Permanent creature = addReadyCreature(player2);
        creature.tap();

        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(creature.getId());

        // Remove Heavy Arbalest
        gd.playerBattlefields.get(player2.getId()).remove(arbalest);

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Creature should now untap normally
        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Effects stop when equipment is removed =====

    @Test
    @DisplayName("Creature loses granted ability when Heavy Arbalest is removed")
    void creatureLosesAbilityWhenRemoved() {
        Permanent creature = addReadyCreature(player1);

        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        // Remove Heavy Arbalest
        gd.playerBattlefields.get(player1.getId()).remove(arbalest);

        // Creature should no longer have an activated ability
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Equipment itself still untaps =====

    @Test
    @DisplayName("Heavy Arbalest itself still untaps during untap step")
    void arbalestItselfStillUntaps() {
        Permanent arbalest = addArbalestReady(player1);
        arbalest.tap();

        advanceToNextTurn(player2);

        // The equipment itself should untap (the effect only prevents the equipped creature from untapping)
        assertThat(arbalest.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addArbalestReady(Player player) {
        Permanent perm = new Permanent(new HeavyArbalest());
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

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
