package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkLooter.class, Forest.class, GrizzlyBears.class, Mountain.class})
class MerfolkLooterTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Merfolk Looter puts it on the stack")
    void castingPutsOnStack() {
        MerfolkLooter card = new MerfolkLooter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Resolving puts Merfolk Looter onto the battlefield")
    void resolvingPutsOnBattlefield() {
        MerfolkLooter card = new MerfolkLooter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new MerfolkLooter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        MerfolkLooter card = new MerfolkLooter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent looter = addCreatureReady(player1, new MerfolkLooter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(looter.getId());
    }

    @Test
    @DisplayName("Activating ability taps Merfolk Looter")
    void activatingTapsLooter() {
        Permanent looter = addCreatureReady(player1, new MerfolkLooter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(looter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent looter = addCreatureReady(player1, new MerfolkLooter());
        looter.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent looter = harness.addToBattlefieldAndReturn(player1, new MerfolkLooter());
        looter.setSummoningSick(true);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Looting ability resolution =====

    @Test
    @DisplayName("Resolving draws a card then prompts for discard")
    void resolvingDrawsThenPromptsForDiscard() {
        addCreatureReady(player1, new MerfolkLooter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // After drawing, hand should have 2 cards (original + drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // Should be awaiting discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());
        assertThat(gameLogContains("draws a card")).isTrue();
    }

    @Test
    @DisplayName("Completing discard moves card to graveyard and hand size stays the same")
    void completingDiscardMovesToGraveyard() {
        addCreatureReady(player1, new MerfolkLooter());
        GrizzlyBears bears = new GrizzlyBears();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(bears));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Hand has [GrizzlyBears, Forest], discard the bears at index 0
        harness.handleCardChosen(player1, 0);

        // Hand should have 1 card (the Forest drawn)
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        // Graveyard should have the discarded card
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        // No longer awaiting input
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("discards")).isTrue();
    }

    @Test
    @DisplayName("Can choose to discard the drawn card instead")
    void canDiscardTheDrawnCard() {
        addCreatureReady(player1, new MerfolkLooter());
        GrizzlyBears bears = new GrizzlyBears();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(bears));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Hand has [GrizzlyBears, Forest], discard the Forest at index 1
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Looting with empty deck still prompts discard if hand has cards")
    void lootingWithEmptyDeckStillDiscardsIfHandHasCards() {
        addCreatureReady(player1, new MerfolkLooter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No card drawn, hand still has 1 card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Should still be awaiting discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gameLogContains("no cards to draw")).isTrue();
    }

    @Test
    @DisplayName("Looting with empty deck and empty hand skips discard")
    void lootingWithEmptyDeckAndEmptyHandSkipsDiscard() {
        addCreatureReady(player1, new MerfolkLooter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No card drawn, hand still empty - discard should be skipped
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no cards to discard")).isTrue();
    }

    @Test
    @DisplayName("With an empty starting hand, discards the card it drew")
    void discardsDrawnCardWhenStartingHandIsEmpty() {
        addCreatureReady(player1, new MerfolkLooter());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Net card count stays the same after full loot cycle")
    void netCardCountStaysSame() {
        addCreatureReady(player1, new MerfolkLooter());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        // Hand size should remain the same (drew 1, discarded 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    // ===== Combat =====

    @Test
    @DisplayName("Unblocked Merfolk Looter deals 1 damage to defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new MerfolkLooter());
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

}

