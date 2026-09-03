package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EnergyBolt;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReflectDamage.class, FemerefScouts.class, EnergyBolt.class})
class ReflectDamageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reflect Damage prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castReflectDamage(player1);
        addReadySource(player2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot redirection shield")
    void choosingSourceRecordsShield() {
        castReflectDamage(player1);
        Permanent source = addReadySource(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.reflectDamageToSourceControllerShields).contains(source.getId());
    }

    @Test
    @DisplayName("The chosen attacker's combat damage is dealt to its own controller instead")
    void redirectsCombatDamageToSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castReflectDamage(player1);
        Permanent source = addReadySource(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.reflectDamageToSourceControllerShields).isEmpty();
    }

    @Test
    @DisplayName("A different source deals its damage normally and the shield is untouched")
    void differentSourceNotRedirected() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castReflectDamage(player1);
        Permanent chosen = addReadySource(player2);
        Permanent other = addReadySource(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.reflectDamageToSourceControllerShields).contains(chosen.getId());
    }

    @Test
    @DisplayName("Only the next damage event is redirected")
    void onlyNextDamageEventIsRedirected() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castReflectDamage(player1);
        Permanent source = addReadySource(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        source.setAttacking(true);
        resolveCombat(player2);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castReflectDamage(player1);
        Permanent source = addReadySource(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.reflectDamageToSourceControllerShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.reflectDamageToSourceControllerShields).isEmpty();
    }

    @Test
    @DisplayName("A spell on the stack can be chosen as the source")
    void spellOnStackCanBeChosenAsSource() {
        harness.setHand(player1, List.of(new EnergyBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 1, player2.getId(), List.of());

        castReflectDamage(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private void castReflectDamage(Player player) {
        harness.castFromHand(player, new ReflectDamage(), "{3}{R}{W}");
    }

    private Permanent addReadySource(Player player) {
        return addCreatureReady(player, new FemerefScouts());
    }
}
