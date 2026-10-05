package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchOfTheReturned.class, BronzeSable.class, TravelersAmulet.class})
class MarchOfTheReturnedTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two target creature cards from the graveyard to hand")
    void returnsTwoTargetCreatureCardsToHand() {
        Card creature1 = new BronzeSable();
        Card creature2 = new BronzeSable();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature1.getId(), creature2.getId());

        List<UUID> selected = new ArrayList<>(choice.validCardIds());
        harness.handleMultipleCardsChosen(player1, selected);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(creature1.getId(), creature2.getId());
    }

    @Test
    @DisplayName("Allows choosing only one creature card")
    void returnsOneTargetCreatureCard() {
        Card creature1 = new BronzeSable();
        Card creature2 = new BronzeSable();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature2.getId())
                .doesNotContain(creature1.getId());
    }

    @Test
    @DisplayName("Only creature cards are valid targets")
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new BronzeSable();
        Card artifact = new TravelersAmulet();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Choosing zero targets returns nothing")
    void choosingZeroTargetsReturnsNothing() {
        Card creature = new BronzeSable();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
    }

    @Test
    @DisplayName("Can be cast with no creatures in the controller's graveyard")
    void canBeCastWithoutLegalTargets() {
        Card opponentCreature = new BronzeSable();
        harness.setGraveyard(player1, List.of(new TravelersAmulet()));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "March of the Returned");
        harness.assertInGraveyard(player1, "Traveler's Amulet");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's creature cards cannot be selected")
    void onlyOffersControllersGraveyard() {
        Card ownCreature = new BronzeSable();
        Card opponentCreature = new BronzeSable();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
    }

    @Test
    @DisplayName("Returns the remaining target when the other leaves the graveyard")
    void returnsRemainingLegalTarget() {
        Card creature1 = new BronzeSable();
        Card creature2 = new BronzeSable();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.setGraveyard(player1, List.of(creature2));
        harness.setExile(player1, List.of(creature1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature2);
        assertThat(gd.findExiledCard(creature1.getId())).isNotNull();
        harness.assertInGraveyard(player1, "March of the Returned");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns nothing when all chosen targets leave the graveyard")
    void allTargetsLeavingGraveyardPreventsReturn() {
        Card creature = new BronzeSable();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.assertInGraveyard(player1, "March of the Returned");
        assertThat(gd.stack).isEmpty();
    }
}
