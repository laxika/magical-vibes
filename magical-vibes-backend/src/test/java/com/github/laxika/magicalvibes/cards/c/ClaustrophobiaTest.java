package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClaustrophobiaTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Claustrophobia has correct effects")
    void hasCorrectEffects() {
        Claustrophobia card = new Claustrophobia();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(TapTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(AttachedCreatureDoesntUntapEffect.class);
    }

    // ===== ETB tap effect =====

    @Test
    @DisplayName("Resolving Claustrophobia taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2);
        assertThat(creature.isTapped()).isFalse();

        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        // Creature should be tapped by the ETB effect
        assertThat(creature.isTapped()).isTrue();
        // Claustrophobia should be attached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Claustrophobia")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Resolving Claustrophobia on already tapped creature keeps it tapped")
    void resolvingOnAlreadyTappedCreature() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Enchanted creature does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent claustrophobiaPerm = new Permanent(new Claustrophobia());
        claustrophobiaPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(claustrophobiaPerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherCreaturesStillUntap() {
        Permanent enchantedCreature = addCreatureReady(player2);
        enchantedCreature.tap();

        Permanent freeCreature = addCreatureReady(player2);
        freeCreature.tap();

        Permanent claustrophobiaPerm = new Permanent(new Claustrophobia());
        claustrophobiaPerm.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(claustrophobiaPerm);

        advanceToNextTurn(player1);

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(freeCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature stays tapped across multiple turns")
    void creatureStaysTappedAcrossMultipleTurns() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent claustrophobiaPerm = new Permanent(new Claustrophobia());
        claustrophobiaPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(claustrophobiaPerm);

        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player2);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Removal restores untapping =====

    @Test
    @DisplayName("Creature can untap again after Claustrophobia is removed")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent claustrophobiaPerm = new Permanent(new Claustrophobia());
        claustrophobiaPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(claustrophobiaPerm);

        // Remove Claustrophobia
        gd.playerBattlefields.get(player1.getId()).remove(claustrophobiaPerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Claustrophobia fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Claustrophobia"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Claustrophobia"));
    }

    // ===== Full integration =====

    @Test
    @DisplayName("Full integration: cast Claustrophobia, creature gets tapped, stays tapped through untap step")
    void fullIntegration() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        // Creature should be tapped by ETB
        assertThat(creature.isTapped()).isTrue();

        // Advance to player2's turn — creature should not untap
        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addCreatureReady(Player player) {
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
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
