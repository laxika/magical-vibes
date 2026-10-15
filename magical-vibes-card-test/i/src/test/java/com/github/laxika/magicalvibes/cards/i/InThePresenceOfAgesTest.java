package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RiverHeraldGuide;
import com.github.laxika.magicalvibes.cards.p.PrimordialGnawer;
import com.github.laxika.magicalvibes.cards.s.StaggeringSize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InThePresenceOfAges.class, Forest.class, RiverHeraldGuide.class, PrimordialGnawer.class, StaggeringSize.class})
class InThePresenceOfAgesTest extends BaseCardTest {

    @Test
    @DisplayName("First pick offers only creatures and the second pick only offers lands")
    void offersOneCreatureAndOneLand() {
        List<Card> topCards = setupTopFour(new RiverHeraldGuide(), new StaggeringSize(), new Forest(), new PrimordialGnawer());

        resolveSpell();

        GameData gd = harness.getGameData();
        assertThat(searchCards(gd)).extracting(Card::getId)
                .containsExactlyInAnyOrder(topCards.get(0).getId(), topCards.get(3).getId());

        harness.handleCardChosen(player1, 0);

        assertThat(searchCards(gd)).extracting(Card::getId).containsExactly(topCards.get(2).getId());
    }

    @Test
    @DisplayName("Chosen creature and land go to hand and the rest go to the graveyard")
    void takesCreatureAndLandRestToGraveyard() {
        List<Card> topCards = setupTopFour(new RiverHeraldGuide(), new StaggeringSize(), new Forest(), new PrimordialGnawer());

        resolveSpell();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .contains(topCards.get(0).getId(), topCards.get(2).getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(topCards.get(1).getId(), topCards.get(3).getId());
    }

    @Test
    @DisplayName("The second pick cannot choose another creature")
    void cannotTakeTwoCreatures() {
        List<Card> topCards = setupTopFour(new RiverHeraldGuide(), new StaggeringSize(), new Forest(), new PrimordialGnawer());

        resolveSpell();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        assertThat(searchCards(gd)).extracting(Card::getId).containsExactly(topCards.get(2).getId());
    }

    @Test
    @DisplayName("Declining both picks puts all revealed cards into the graveyard")
    void decliningBothBinsEverything() {
        List<Card> topCards = setupTopFour(new RiverHeraldGuide(), new StaggeringSize(), new Forest(), new PrimordialGnawer());

        resolveSpell();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(topCards.get(0).getId(), topCards.get(1).getId(),
                        topCards.get(2).getId(), topCards.get(3).getId());
    }

    @Test
    void canTakeOnlyLandAfterDecliningCreature() {
        List<Card> cards = setupTopFour(new RiverHeraldGuide(), new Forest(), new StaggeringSize(), new PrimordialGnawer());
        resolveSpell();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(cards.get(1));
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .contains(cards.get(0), cards.get(2), cards.get(3));
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    void canTakeOnlyCreatureAfterDecliningLand() {
        List<Card> cards = setupTopFour(new RiverHeraldGuide(), new Forest(), new StaggeringSize(), new PrimordialGnawer());
        resolveSpell();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(cards.get(0));
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .contains(cards.get(1), cards.get(2), cards.get(3));
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    void skipsCreatureChoiceWhenOnlyLandsAreEligible() {
        List<Card> cards = setupTopFour(new StaggeringSize(), new Forest(), new Forest(), new StaggeringSize());
        resolveSpell();
        assertThat(searchCards(harness.getGameData())).containsExactly(cards.get(1), cards.get(2));
        harness.handleCardChosen(player1, 1);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(cards.get(2));
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .contains(cards.get(0), cards.get(1), cards.get(3));
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryWithoutLandFinishesAfterCreatureChoice() {
        List<Card> cards = setupTopFour(new RiverHeraldGuide(), new StaggeringSize());
        resolveSpell();
        harness.handleCardChosen(player1, 0);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(cards.get(0));
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).contains(cards.get(1));
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    void leavesFifthCardInLibraryWhenNothingIsEligible() {
        List<Card> cards = setupTopFour(new StaggeringSize(), new StaggeringSize(),
                new StaggeringSize(), new StaggeringSize(), new Forest());
        resolveSpell();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(cards.get(4));
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .contains(cards.get(0), cards.get(1), cards.get(2), cards.get(3));
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutChoice() {
        setupTopFour();
        resolveSpell();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private void resolveSpell() {
        harness.setHand(player1, List.of(new InThePresenceOfAges()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0);
    }

    private List<Card> searchCards(GameData gd) {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().toList();
    }

    private List<Card> setupTopFour(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
        return List.of(cards);
    }
}
