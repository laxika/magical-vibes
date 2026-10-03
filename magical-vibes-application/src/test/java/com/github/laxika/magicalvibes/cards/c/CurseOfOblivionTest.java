package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BoneyardWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfOblivion.class, BoneyardWurm.class})
class CurseOfOblivionTest extends BaseCardTest {


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


    @Test
    @DisplayName("Enchanted player must exile 2 cards from graveyard at their upkeep")
    void enchantedPlayerExilesTwoCardsAtUpkeep() {
        placeCurseOnPlayer(player1, player2);
        Card bears1 = new BoneyardWurm();
        Card bears2 = new BoneyardWurm();
        Card bears3 = new BoneyardWurm();
        harness.setGraveyard(player2, List.of(bears1, bears2, bears3));

        advanceToUpkeep(player2);
        // Trigger is on the stack, resolve it
        harness.passBothPriorities();

        // Should be awaiting graveyard choice (3 cards > 2 required)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class) != null).isTrue();

        // Choose first card to exile
        harness.handleGraveyardCardChosen(player2, 0);
        // Second choice prompted
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class) != null).isTrue();
        harness.handleGraveyardCardChosen(player2, 0);

        // Both exiled, one remains in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }


    @Test
    @DisplayName("Auto-exiles all cards when graveyard has exactly 2 cards")
    void autoExilesWhenGraveyardHasExactlyTwoCards() {
        placeCurseOnPlayer(player1, player2);
        Card bears1 = new BoneyardWurm();
        Card bears2 = new BoneyardWurm();
        harness.setGraveyard(player2, List.of(bears1, bears2));

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger â€” auto-exiles both

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Auto-exiles all cards when graveyard has only 1 card")
    void autoExilesWhenGraveyardHasOneCard() {
        placeCurseOnPlayer(player1, player2);
        Card bears = new BoneyardWurm();
        harness.setGraveyard(player2, List.of(bears));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }


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


    @Test
    @DisplayName("Trigger does NOT fire during curse controller's upkeep")
    void triggerDoesNotFireDuringCurseControllerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        Card bears = new BoneyardWurm();
        harness.setGraveyard(player1, List.of(bears));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1's graveyard should be untouched
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }


    @Test
    @DisplayName("No exile trigger after Curse of Oblivion is removed")
    void noTriggerAfterRemoval() {
        Permanent cursePerm = placeCurseOnPlayer(player1, player2);
        Card bears = new BoneyardWurm();
        harness.setGraveyard(player2, List.of(bears));

        // Remove the curse
        gd.playerBattlefields.get(player1.getId()).remove(cursePerm);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }


    @Test
    @DisplayName("A player can enchant themselves and exile from their own graveyard")
    void canEnchantSelf() {
        harness.setHand(player1, List.of(new CurseOfOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        Card card = new BoneyardWurm();
        harness.setGraveyard(player1, List.of(card));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing the curse after its trigger is stacked does not prevent exile")
    void triggerResolvesAfterCurseLeavesBattlefield() {
        Permanent curse = placeCurseOnPlayer(player1, player2);
        Card card = new BoneyardWurm();
        harness.setGraveyard(player2, List.of(card));

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The enchanted player chooses which cards to exile and the controller's graveyard is untouched")
    void enchantedPlayerChoosesCardsFromTheirOwnGraveyard() {
        placeCurseOnPlayer(player1, player2);
        Card first = new BoneyardWurm();
        Card second = new BoneyardWurm();
        Card third = new BoneyardWurm();
        Card controllerCard = new BoneyardWurm();
        harness.setGraveyard(player2, List.of(first, second, third));
        harness.setGraveyard(player1, List.of(controllerCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player2, 2);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, new CurseOfOblivion());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }
}
