package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FootbottomFeast.class, GrizzlyBears.class, GiantSpider.class, HolyDay.class, LightningBolt.class})
class FootbottomFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with creature cards in graveyard prompts for target selection")
    void castingWithCreaturesInGraveyardPromptsTargetSelection() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GiantSpider();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.castFromHand(player1, new FootbottomFeast(), "{2}{B}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selected creature card is put on top and drawn")
    void selectedCreatureCardIsDrawn() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new FootbottomFeast(), "{2}{B}");

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName()).isEqualTo("Footbottom Feast");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Selecting zero targets still draws a card")
    void selectingZeroTargetsStillDraws() {
        Card graveyardCreature = new GrizzlyBears();
        Card topCard = new GiantSpider();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new FootbottomFeast(), "{2}{B}");
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()))
                .anyMatch(c -> c.getName().equals("Footbottom Feast"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Only creature cards in your graveyard are valid targets")
    void onlyCreatureCardsInYourGraveyardAreValidTargets() {
        Card creature = new GrizzlyBears();
        Card nonCreature = new LightningBolt();
        Card opponentCreature = new GiantSpider();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.castFromHand(player1, new FootbottomFeast(), "{2}{B}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Casting with no creature cards in graveyard skips target prompt and still draws")
    void castingWithNoCreaturesSkipsPromptAndDraws() {
        Card topCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new HolyDay()));
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new FootbottomFeast(), "{2}{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player1, "Footbottom Feast");
    }
    @Test
    @DisplayName("Multiple returned creatures are ordered before drawing the chosen top card")
    void ordersReturnedCreaturesBeforeDrawing() {
        Card first = new GrizzlyBears();
        Card second = new GiantSpider();
        Card originalTop = new LightningBolt();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(originalTop));
        harness.castFromHand(player1, new FootbottomFeast(), "{2}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, originalTop);
        harness.assertInGraveyard(player1, "Footbottom Feast");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All targets leaving the graveyard prevents the draw")
    void allTargetsLeavingGraveyardPreventsDraw() {
        Card creature = new GrizzlyBears();
        Card originalTop = new GiantSpider();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(originalTop));
        harness.castFromHand(player1, new FootbottomFeast(), "{2}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        harness.assertInGraveyard(player1, "Footbottom Feast");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A remaining legal target is returned and drawn when another target leaves")
    void remainingLegalTargetIsReturnedAndDrawn() {
        Card removed = new GrizzlyBears();
        Card remaining = new GiantSpider();
        Card originalTop = new LightningBolt();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setLibrary(player1, List.of(originalTop));
        harness.castFromHand(player1, new FootbottomFeast(), "{2}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        harness.assertInGraveyard(player1, "Footbottom Feast");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
