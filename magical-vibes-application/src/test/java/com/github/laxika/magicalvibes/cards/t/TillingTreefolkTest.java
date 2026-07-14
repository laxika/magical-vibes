package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TillingTreefolkTest extends BaseCardTest {

    private void castTreefolk() {
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB prompts to return up to two land cards from graveyard")
    void etbPromptsForLands() {
        harness.setGraveyard(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);
    }

    @Test
    @DisplayName("Returning two lands puts both in hand")
    void returnsTwoLandsToHand() {
        harness.setGraveyard(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Forest"))
                .noneMatch(c -> c.getName().equals("Mountain"));
    }

    @Test
    @DisplayName("Only land cards are valid targets")
    void onlyLandsAreValidTargets() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land, new GrizzlyBears()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Choosing zero targets returns nothing")
    void choosingZeroReturnsNothing() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("No land cards in graveyard: enters with no prompt")
    void noLandsNoPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tilling Treefolk"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
