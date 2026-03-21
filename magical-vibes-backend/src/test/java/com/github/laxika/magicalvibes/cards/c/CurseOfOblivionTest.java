package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromOwnGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfOblivionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Curse of Oblivion has correct effects")
    void hasCorrectEffects() {
        CurseOfOblivion card = new CurseOfOblivion();

        assertThat(card.getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ExileCardsFromOwnGraveyardEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Can cast Curse of Oblivion targeting a player")
    void canCastTargetingPlayer() {
        harness.setHand(player1, List.of(new CurseOfOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Curse of Oblivion attaches it to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Oblivion")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    // ===== Upkeep trigger with graveyard > 2 cards =====

    @Test
    @DisplayName("Enchanted player must exile 2 cards from graveyard at their upkeep")
    void enchantedPlayerExilesTwoCardsAtUpkeep() {
        placeCurseOnPlayer(player1, player2);
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears1, bears2, bears3));

        advanceToUpkeep(player2);
        // Trigger is on the stack, resolve it
        harness.passBothPriorities();

        // Should be awaiting graveyard choice (3 cards > 2 required)
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)).isTrue();

        // Choose first card to exile
        harness.handleGraveyardCardChosen(player2, 0);
        // Second choice prompted
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)).isTrue();
        harness.handleGraveyardCardChosen(player2, 0);

        // Both exiled, one remains in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    // ===== Upkeep trigger with graveyard <= 2 cards =====

    @Test
    @DisplayName("Auto-exiles all cards when graveyard has exactly 2 cards")
    void autoExilesWhenGraveyardHasExactlyTwoCards() {
        placeCurseOnPlayer(player1, player2);
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears1, bears2));

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger — auto-exiles both

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Auto-exiles all cards when graveyard has only 1 card")
    void autoExilesWhenGraveyardHasOneCard() {
        placeCurseOnPlayer(player1, player2);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    // ===== Empty graveyard =====

    @Test
    @DisplayName("Does nothing when enchanted player has empty graveyard")
    void doesNothingWithEmptyGraveyard() {
        placeCurseOnPlayer(player1, player2);
        harness.setGraveyard(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    // ===== Trigger timing =====

    @Test
    @DisplayName("Trigger does NOT fire during curse controller's upkeep")
    void triggerDoesNotFireDuringCurseControllerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1's graveyard should be untouched
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    // ===== Removal =====

    @Test
    @DisplayName("No exile trigger after Curse of Oblivion is removed")
    void noTriggerAfterRemoval() {
        Permanent cursePerm = placeCurseOnPlayer(player1, player2);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        // Remove the curse
        gd.playerBattlefields.get(player1.getId()).remove(cursePerm);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    // ===== Helpers =====

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = new Permanent(new CurseOfOblivion());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(cursePerm);
        return cursePerm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
