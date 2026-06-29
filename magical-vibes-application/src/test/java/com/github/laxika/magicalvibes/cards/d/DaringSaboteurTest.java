package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaringSaboteurTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has activated ability with {2}{U} cost and MakeCreatureUnblockableEffect")
    void hasUnblockableActivatedAbility() {
        DaringSaboteur card = new DaringSaboteur();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}{U}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(MakeCreatureUnblockableEffect.class);
        MakeCreatureUnblockableEffect effect = (MakeCreatureUnblockableEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.selfTargeting()).isTrue();
    }

    @Test
    @DisplayName("Has ON_COMBAT_DAMAGE_TO_PLAYER MayEffect wrapping DrawAndDiscardCardEffect")
    void hasCombatDamageTrigger() {
        DaringSaboteur card = new DaringSaboteur();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DrawAndDiscardCardEffect.class);
    }

    // ===== Activated ability: make self unblockable =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent saboteur = addSaboteurReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Daring Saboteur");
        assertThat(entry.getTargetId()).isEqualTo(saboteur.getId());
    }

    @Test
    @DisplayName("Resolving ability makes Daring Saboteur unblockable this turn")
    void resolvingAbilityMakesUnblockable() {
        Permanent saboteur = addSaboteurReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(saboteur.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable resets at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent saboteur = addSaboteurReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(saboteur.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(saboteur.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Daring Saboteur")
    void activatingAbilityDoesNotTap() {
        Permanent saboteur = addSaboteurReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(saboteur.isTapped()).isFalse();
    }

    // ===== Combat damage trigger: accept may =====

    @Test
    @DisplayName("Deals combat damage to player and controller accepts loot — draws then discards")
    void combatDamageAcceptMay() {
        Permanent saboteur = addSaboteurReady(player1);
        saboteur.setAttacking(true);
        harness.setLife(player2, 20);

        setDeck(player1, List.of(new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // May ability prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Drew a card, now awaiting discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        // Discard a card
        harness.handleCardChosen(player1, 0);

        // Net: drew 1, discarded 1 — hand size stays the same
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Combat damage trigger: decline may =====

    @Test
    @DisplayName("Deals combat damage to player and controller declines loot — no draw or discard")
    void combatDamageDeclineMay() {
        Permanent saboteur = addSaboteurReady(player1);
        saboteur.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No change in hand size
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== No trigger when blocked =====

    @Test
    @DisplayName("No trigger when Daring Saboteur is blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent saboteur = addSaboteurReady(player1);
        saboteur.setAttacking(true);

        // 4/4 blocker kills the 2/1 Daring Saboteur
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Daring Saboteur should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Daring Saboteur"));

        // No trigger — didn't deal combat damage to a player
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Helpers =====

    private Permanent addSaboteurReady(Player player) {
        Permanent perm = new Permanent(new DaringSaboteur());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
