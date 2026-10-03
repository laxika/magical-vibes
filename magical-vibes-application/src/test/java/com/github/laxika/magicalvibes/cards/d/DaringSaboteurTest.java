package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ShiningAerosaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaringSaboteur.class, Forest.class, ShiningAerosaur.class})
class DaringSaboteurTest extends BaseCardTest {

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

    @Test
    @DisplayName("Deals combat damage to player and controller accepts loot — draws then discards")
    void combatDamageAcceptMay() {
        Permanent saboteur = addSaboteurReady(player1);
        saboteur.setAttacking(true);
        harness.setLife(player2, 20);

        harness.setLibrary(player1, List.of(new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // May ability prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Drew a card, now awaiting discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        // Discard a card
        harness.handleCardChosen(player1, 0);

        // Net: drew 1, discarded 1 — hand size stays the same
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

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

    @Test
    @DisplayName("No trigger when Daring Saboteur is blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent saboteur = addSaboteurReady(player1);
        saboteur.setAttacking(true);

        // 3/4 blocker kills the 2/1 Daring Saboteur
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ShiningAerosaur());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Daring Saboteur should be dead
        harness.assertInGraveyard(player1, "Daring Saboteur");

        // No trigger — didn't deal combat damage to a player
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Loot may discard a previously held card and keep the drawn card")
    void lootCanDiscardPreviouslyHeldCard() {
        Permanent saboteur = addSaboteurReady(player1);
        saboteur.setAttacking(true);
        Forest heldCard = new Forest();
        ShiningAerosaur drawnCard = new ShiningAerosaur();
        harness.setHand(player1, List.of(heldCard));
        harness.setLibrary(player1, List.of(drawnCard, new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(heldCard, drawnCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(heldCard);
    }

    @Test
    @DisplayName("Unblockable ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent saboteur = harness.addToBattlefieldAndReturn(player1, new DaringSaboteur());
        saboteur.setTapped(true);
        saboteur.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(saboteur.isCantBeBlocked()).isTrue();
        assertThat(saboteur.isTapped()).isTrue();
    }

    private Permanent addSaboteurReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DaringSaboteur());
        perm.setSummoningSick(false);
        return perm;
    }
}
