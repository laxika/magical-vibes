package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntanglingVinesTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Entangling Vines has correct card properties")
    void hasCorrectProperties() {
        EntanglingVines card = new EntanglingVines();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(AttachedCreatureDoesntUntapEffect.class);
    }

    // ===== Targeting restriction: must target tapped creature =====

    @Test
    @DisplayName("Can target a tapped creature with Entangling Vines")
    void canTargetTappedCreature() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an untapped creature with Entangling Vines")
    void cannotTargetUntappedCreature() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Entangling Vines")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();
        artifact.tap();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Entangling Vines puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Entangling Vines");
    }

    @Test
    @DisplayName("Resolving Entangling Vines attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Entangling Vines")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Tapped creature with Entangling Vines does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent vinesPerm = new Permanent(new EntanglingVines());
        vinesPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(vinesPerm);

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other permanents owned by the same player still untap normally")
    void otherPermanentsStillUntap() {
        Permanent enchantedBears = new Permanent(new GrizzlyBears());
        enchantedBears.setSummoningSick(false);
        enchantedBears.tap();
        gd.playerBattlefields.get(player2.getId()).add(enchantedBears);

        Permanent freeBears = new Permanent(new GrizzlyBears());
        freeBears.setSummoningSick(false);
        freeBears.tap();
        gd.playerBattlefields.get(player2.getId()).add(freeBears);

        Permanent vinesPerm = new Permanent(new EntanglingVines());
        vinesPerm.setAttachedTo(enchantedBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(vinesPerm);

        advanceToNextTurn(player1);

        assertThat(enchantedBears.isTapped()).isTrue();
        assertThat(freeBears.isTapped()).isFalse();
    }

    // ===== Removal restores untapping =====

    @Test
    @DisplayName("Creature can untap again after Entangling Vines is removed")
    void creatureUntapsAfterVinesRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent vinesPerm = new Permanent(new EntanglingVines());
        vinesPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(vinesPerm);

        gd.playerBattlefields.get(player1.getId()).remove(vinesPerm);

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    // ===== Full integration =====

    @Test
    @DisplayName("Full integration: cast Entangling Vines on tapped creature, advance turn, creature stays tapped")
    void fullIntegrationCastAndPreventUntap() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Entangling Vines")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
