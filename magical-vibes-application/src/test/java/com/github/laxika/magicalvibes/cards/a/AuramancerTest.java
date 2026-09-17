package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Auramancer.class, GroundSeal.class, AngelicWall.class})
class AuramancerTest extends BaseCardTest {

    private void castAuramancer() {
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns the chosen enchantment card to hand")
    void returnsEnchantmentToHand() {
        harness.setGraveyard(player1, List.of(new GroundSeal()));
        harness.setHand(player1, List.of(new Auramancer()));

        castAuramancer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1,
                List.copyOf(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ground Seal");
        harness.assertNotInGraveyard(player1, "Ground Seal");
    }

    @Test
    @DisplayName("Only enchantment cards are valid targets")
    void onlyEnchantmentsAreValid() {
        Card enchantment = new GroundSeal();
        harness.setGraveyard(player1, List.of(enchantment, new AngelicWall()));
        harness.setHand(player1, List.of(new Auramancer()));

        castAuramancer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(enchantment.getId());
    }

    @Test
    @DisplayName("Declining the optional return leaves the card in the graveyard")
    void decliningReturnsNothing() {
        harness.setGraveyard(player1, List.of(new GroundSeal()));
        harness.setHand(player1, List.of(new Auramancer()));

        castAuramancer();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ground Seal");
    }

    @Test
    @DisplayName("No enchantment cards in graveyard: enters with no prompt")
    void noEnchantmentsNoPrompt() {
        harness.setGraveyard(player1, List.of(new AngelicWall()));
        harness.setHand(player1, List.of(new Auramancer()));

        castAuramancer();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Auramancer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the controller's graveyard is searched")
    void onlyControllersGraveyardIsSearched() {
        harness.setGraveyard(player2, List.of(new GroundSeal()));
        harness.setHand(player1, List.of(new Auramancer()));

        castAuramancer();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Ground Seal");
        harness.assertInGraveyard(player2, "Ground Seal");
    }

    @Test
    @DisplayName("The ETB returns at most one enchantment card")
    void returnsAtMostOneEnchantment() {
        GroundSeal first = new GroundSeal();
        GroundSeal second = new GroundSeal();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Auramancer()));

        castAuramancer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ground Seal");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }
}
