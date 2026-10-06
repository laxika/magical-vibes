package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EnergyBolt;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.s.SabertoothCobra;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReflectDamage.class, FemerefScouts.class, EnergyBolt.class, SabertoothCobra.class})
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

    @Test
    @DisplayName("Damage from a chosen spell is redirected to that spell's controller")
    void redirectsDamageFromSpellOnStack() {
        EnergyBolt bolt = new EnergyBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 1,
                player2.getId(), List.of());

        castReflectDamage(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, bolt.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Redirected combat damage still triggers the chosen source's damage ability")
    void redirectedDamageTriggersSourceAbilityForItsController() {
        Permanent cobra = addCreatureReady(player2, new SabertoothCobra());
        castReflectDamage(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, cobra.getId());

        cobra.setAttacking(true);
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("All simultaneous damage from a chosen attacker is redirected")
    void redirectsDamageToBothBlockers() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        Permanent firstBlocker = addReadySource(player2);
        Permanent secondBlocker = addReadySource(player2);
        castReflectDamage(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, cobra.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1, secondBlocker.getId(), 1));

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isZero();
    }

    private void castReflectDamage(Player player) {
        harness.castFromHand(player, new ReflectDamage(), "{3}{R}{W}");
    }

    private Permanent addReadySource(Player player) {
        return addCreatureReady(player, new FemerefScouts());
    }
}
