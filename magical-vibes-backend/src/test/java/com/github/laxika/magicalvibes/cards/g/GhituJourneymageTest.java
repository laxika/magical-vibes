package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GhituJourneymageTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has controls-another-Wizard conditional ETB damage effect")
    void hasConditionalEtbEffect() {
        GhituJourneymage card = new GhituJourneymage();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ControlsAnotherSubtypeConditionalEffect.class);

        ControlsAnotherSubtypeConditionalEffect conditional =
                (ControlsAnotherSubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(conditional.subtypes()).isEqualTo(Set.of(CardSubtype.WIZARD));
        assertThat(conditional.wrapped()).isInstanceOf(DealDamageToEachOpponentEffect.class);

        DealDamageToEachOpponentEffect damage = (DealDamageToEachOpponentEffect) conditional.wrapped();
        assertThat(damage.damage()).isEqualTo(2);
    }

    // ===== ETB with another Wizard =====

    @Test
    @DisplayName("ETB triggers when you control another Wizard")
    void etbTriggersWithAnotherWizard() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ghitu Journeymage");
    }

    @Test
    @DisplayName("ETB deals 2 damage to each opponent when another Wizard is controlled")
    void etbDealsDamageWithAnotherWizard() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB deals 2 damage with non-default life totals")
    void etbDealsDamageWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.addToBattlefield(player1, new FugitiveWizard());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    // ===== ETB without another Wizard =====

    @Test
    @DisplayName("ETB does NOT trigger without another Wizard (only self)")
    void etbDoesNotTriggerWithoutAnotherWizard() {
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ghitu Journeymage"));

        // Life totals unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does NOT trigger when opponent controls a Wizard but you don't")
    void etbDoesNotTriggerWithOpponentWizard() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger
        assertThat(gd.stack).isEmpty();

        // Life totals unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Condition lost before resolution =====

    @Test
    @DisplayName("ETB does nothing if the other Wizard is removed before resolution")
    void etbFizzlesWhenAnotherWizardRemoved() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove the other Wizard before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Fugitive Wizard"));

        harness.passBothPriorities(); // resolve ETB trigger — condition no longer met

        // Life totals unchanged (ability does nothing)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("controls another Wizard ability does nothing"));
    }

    // ===== Creature enters battlefield regardless =====

    @Test
    @DisplayName("Creature enters battlefield even without another Wizard")
    void creatureEntersWithoutAnotherWizard() {
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ghitu Journeymage"));
    }

    @Test
    @DisplayName("Stack is empty after full resolution with another Wizard")
    void stackEmptyAfterResolution() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Multiple Wizards =====

    @Test
    @DisplayName("ETB triggers with two other Wizards — still deals only 2 damage")
    void etbTriggersWithMultipleWizards() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Helpers =====

    private void castGhituJourneymage() {
        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
    }
}
