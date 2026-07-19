package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ControlledInstinctsTest extends BaseCardTest {

    // ===== Targeting restriction: red or green creature =====

    @Test
    @DisplayName("Can enchant a red creature")
    void canEnchantRedCreature() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Controlled Instincts")
                        && p.isAttached()
                        && p.getAttachedTo().equals(giant.getId()));
    }

    @Test
    @DisplayName("Can enchant a green creature")
    void canEnchantGreenCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Controlled Instincts")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a creature that is neither red nor green")
    void cannotEnchantOffColorCreature() {
        // A legal green target exists so the card is playable; the blue creature is rejected.
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player2.getId()).add(wizard);

        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a red or green creature");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        // A legal green target exists so the card is playable; the noncreature is rejected.
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));

        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.p.Pacifism());
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pacifism"))
                .findFirst().orElseThrow();

        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a red or green creature");
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        giant.tap();
        gd.playerBattlefields.get(player2.getId()).add(giant);

        Permanent aura = new Permanent(new ControlledInstincts());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped enchanted creature stays untapped (aura does not tap)")
    void untappedCreatureStaysUntapped() {
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        Permanent aura = new Permanent(new ControlledInstincts());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps again after Controlled Instincts is removed")
    void creatureUntapsAfterAuraRemoved() {
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        giant.tap();
        gd.playerBattlefields.get(player2.getId()).add(giant);

        Permanent aura = new Permanent(new ControlledInstincts());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(giant.isTapped()).isFalse();
    }

    // ===== Helpers =====

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
