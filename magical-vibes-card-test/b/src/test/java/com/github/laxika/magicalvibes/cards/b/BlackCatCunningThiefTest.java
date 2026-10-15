package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PumpkinBombardment;
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

@CardUsed({BlackCatCunningThief.class, GrizzlyBears.class, LightningBolt.class, Swamp.class,
        PumpkinBombardment.class})
class BlackCatCunningThiefTest extends BaseCardTest {

    private void castBlackCat(List<Card> library) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new BlackCatCunningThief()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();
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

    @Test
    void emptyLibraryFinishesWithoutAChoice() {
        castBlackCat(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(harness.getPermanentId(player1, "Black Cat, Cunning Thief")))
                .isEmpty();
    }

    @Test
    void oneCardLibraryExilesItsOnlyCardAndAllowsPlayingIt() {
        Swamp swamp = new Swamp();
        castBlackCat(List.of(swamp));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.castFromExile(player1, swamp.getId());
        harness.assertOnBattlefield(player1, "Swamp");
        assertThat(gd.findExiledCard(swamp.getId())).isNull();
    }

    @Test
    void cardsBelowTopNineStayAboveTheBottomedCards() {
        List<Card> library = List.of(new Swamp(), new Swamp(), new Swamp(), new Swamp(),
                new Swamp(), new Swamp(), new Swamp(), new Swamp(), new Swamp(),
                new Swamp(), new Swamp());
        castBlackCat(library);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyElementsOf(library.subList(0, 9));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2))
                .containsExactly(library.get(9), library.get(10));
        assertThat(gd.playerDecks.get(player2.getId()).subList(2, 9))
                .containsExactlyInAnyOrderElementsOf(library.subList(2, 9));
    }

    @Test
    void secondExiledSpellAlsoAllowsAnyMana() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        castBlackCat(List.of(first, second));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castFromExile(player1, first.getId());
        resolveAllTriggers();
        harness.castFromExile(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    void exiledLandsStillRespectLandDropLimit() {
        Swamp first = new Swamp();
        Swamp second = new Swamp();
        castBlackCat(List.of(first, second));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.castFromExile(player1, first.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    void exiledLandCannotBePlayedOutsideMainPhase() {
        Swamp swamp = new Swamp();
        castBlackCat(List.of(swamp));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, swamp.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(swamp.getId())).isNotNull();
    }

    @Test
    void exiledSpellCanPayAdditionalManaCost() {
        PumpkinBombardment bombardment = new PumpkinBombardment();
        castBlackCat(List.of(bombardment));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.addMana(player1, ManaColor.BLACK, 3);
        UUID targetId = harness.getPermanentId(player1, "Black Cat, Cunning Thief");

        harness.castFromExile(player1, bombardment.getId(), targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Black Cat, Cunning Thief");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bombardment);
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
