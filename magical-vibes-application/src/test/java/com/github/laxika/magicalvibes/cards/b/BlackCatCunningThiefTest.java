package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackCatCunningThief.class, GrizzlyBears.class, LightningBolt.class, Swamp.class})
class BlackCatCunningThiefTest extends BaseCardTest {

    private void castBlackCat(List<Card> library) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new BlackCatCunningThief()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles two of the target opponent's top nine cards face down")
    void etbExilesTwoCardsAndBottomsTheRest() {
        List<Card> library = List.of(new GrizzlyBears(), new Swamp(), new GrizzlyBears(), new Swamp(),
                new GrizzlyBears(), new Swamp(), new GrizzlyBears(), new Swamp(), new GrizzlyBears());
        castBlackCat(library);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        Card first = search.params().cards().get(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int secondIndex = indexOf(search.params().cards(), library.get(1));
        Card second = search.params().cards().get(secondIndex);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(secondIndex));

        UUID sourceId = harness.getPermanentId(player1, "Black Cat, Cunning Thief");
        assertThat(gd.getCardsExiledByPermanent(sourceId)).containsExactlyInAnyOrder(first, second);
        assertThat(gd.getCardsExiledByPermanent(sourceId)).allMatch(card ->
                gd.findExiledCard(card.getId()).faceDown());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiled cards remain playable with any mana after Black Cat leaves")
    void exiledCardsRemainPlayableAfterSourceLeaves() {
        List<Card> library = List.of(new GrizzlyBears(), new Swamp(), new GrizzlyBears(), new Swamp(),
                new GrizzlyBears(), new Swamp(), new GrizzlyBears(), new Swamp(), new GrizzlyBears());
        castBlackCat(library);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int bearsIndex = indexOf(search.params().cards(), library.get(0));
        Card bears = search.params().cards().get(bearsIndex);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(bearsIndex));
        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int secondIndex = indexOf(search.params().cards(), library.get(1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(secondIndex));

        UUID sourceId = harness.getPermanentId(player1, "Black Cat, Cunning Thief");
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, sourceId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Black Cat, Cunning Thief");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bears.getId());
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void etbCannotTargetItsController() {
        harness.setHand(player1, List.of(new BlackCatCunningThief()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(List<Card> cards, Card target) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(target.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card not found in interaction");
    }
}
