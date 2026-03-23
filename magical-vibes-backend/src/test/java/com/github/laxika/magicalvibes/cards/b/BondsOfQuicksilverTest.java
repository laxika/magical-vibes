package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

class BondsOfQuicksilverTest extends BaseCardTest {


    // ===== Card properties =====


    @Test
    @DisplayName("Bonds of Quicksilver has static AttachedCreatureDoesntUntapEffect")
    void hasCorrectProperties() {
        BondsOfQuicksilver card = new BondsOfQuicksilver();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(AttachedCreatureDoesntUntapEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Bonds of Quicksilver puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bonds of Quicksilver");
    }

    @Test
    @DisplayName("Resolving Bonds of Quicksilver attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bonds of Quicksilver")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Tapped creature with Bonds of Quicksilver does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent bondsPerm = new Permanent(new BondsOfQuicksilver());
        bondsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(bondsPerm);

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

        Permanent bondsPerm = new Permanent(new BondsOfQuicksilver());
        bondsPerm.setAttachedTo(enchantedBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(bondsPerm);

        advanceToNextTurn(player1);

        assertThat(enchantedBears.isTapped()).isTrue();
        assertThat(freeBears.isTapped()).isFalse();
    }

    // ===== Removal restores untapping =====

    @Test
    @DisplayName("Creature can untap again after Bonds of Quicksilver is removed")
    void creatureUntapsAfterBondsRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent bondsPerm = new Permanent(new BondsOfQuicksilver());
        bondsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(bondsPerm);

        gd.playerBattlefields.get(player1.getId()).remove(bondsPerm);

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    // ===== Fizzles if target removed =====

    @Test
    @DisplayName("Bonds of Quicksilver fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bonds of Quicksilver"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bonds of Quicksilver"));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Bonds of Quicksilver")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Bonds of Quicksilver")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Full integration =====

    @Test
    @DisplayName("Full integration: cast Bonds on tapped creature, advance turn, creature stays tapped")
    void fullIntegrationCastAndPreventUntap() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new BondsOfQuicksilver()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bonds of Quicksilver")
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
