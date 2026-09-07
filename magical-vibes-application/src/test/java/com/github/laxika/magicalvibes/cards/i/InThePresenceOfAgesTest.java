package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InThePresenceOfAges.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class InThePresenceOfAgesTest extends BaseCardTest {

    @Test
    @DisplayName("First pick offers only creatures and the second pick only offers lands")
    void offersOneCreatureAndOneLand() {
        List<Card> topCards = setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolveSpell();

        GameData gd = harness.getGameData();
        assertThat(searchCards(gd)).extracting(Card::getId)
                .containsExactlyInAnyOrder(topCards.get(0).getId(), topCards.get(3).getId());

        chooseCard(gd, 0);

        assertThat(searchCards(gd)).extracting(Card::getId).containsExactly(topCards.get(2).getId());
    }

    @Test
    @DisplayName("Chosen creature and land go to hand and the rest go to the graveyard")
    void takesCreatureAndLandRestToGraveyard() {
        List<Card> topCards = setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolveSpell();

        GameData gd = harness.getGameData();
        chooseCard(gd, 0);
        chooseCard(gd, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .contains(topCards.get(0).getId(), topCards.get(2).getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(topCards.get(1).getId(), topCards.get(3).getId());
    }

    @Test
    @DisplayName("The second pick cannot choose another creature")
    void cannotTakeTwoCreatures() {
        List<Card> topCards = setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolveSpell();

        GameData gd = harness.getGameData();
        chooseCard(gd, 0);

        assertThat(searchCards(gd)).extracting(Card::getId).containsExactly(topCards.get(2).getId());
    }

    @Test
    @DisplayName("Declining both picks puts all revealed cards into the graveyard")
    void decliningBothBinsEverything() {
        List<Card> topCards = setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolveSpell();

        GameData gd = harness.getGameData();
        chooseCard(gd, -1);
        chooseCard(gd, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(topCards.get(0).getId(), topCards.get(1).getId(),
                        topCards.get(2).getId(), topCards.get(3).getId());
    }

    private void resolveSpell() {
        harness.setHand(player1, List.of(new InThePresenceOfAges()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(GameData gd, int index) {
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<Card> searchCards(GameData gd) {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().toList();
    }

    private List<Card> setupTopFour(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
        return List.of(cards);
    }
}
