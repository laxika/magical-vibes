package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KavuScout;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperateResearch.class, KavuTitan.class, KavuScout.class, Plains.class})
class DesperateResearchTest extends BaseCardTest {

    private void cast() {
        harness.castFromHand(player1, new DesperateResearch(), "{1}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a non-basic-land card")
    void promptsForNonBasicLandName() {
        harness.setLibrary(player1, List.of(new Plains(), new KavuTitan()));

        cast();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseNameRevealTopCardsToHandRestToExileChoice.class);
        assertThat(choice.options()).contains("Kavu Titan").doesNotContain("Plains");
    }

    @Test
    @DisplayName("Offers a legal non-basic-land card name with no copy in the game")
    void offersNonBasicLandNameNotPresentInGame() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        Card absentCardName = new KavuTitan();

        cast();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains(absentCardName.getName());
    }

    @Test
    @DisplayName("Puts all matching cards from the top seven into hand and exiles the rest")
    void matchingCardsGoToHandAndRestAreExiled() {
        UUID p1 = player1.getId();
        Card hit1 = new KavuTitan();
        Card miss1 = new KavuScout();
        Card hit2 = new KavuTitan();
        Card miss2 = new KavuScout();
        Card miss3 = new KavuScout();
        Card miss4 = new KavuScout();
        Card miss5 = new KavuScout();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(hit1, miss1, hit2, miss2, miss3, miss4, miss5, untouched));

        cast();
        harness.handleListChoice(player1, "Kavu Titan");

        assertThat(gd.playerHands.get(p1)).extracting(Card::getId)
                .contains(hit1.getId(), hit2.getId())
                .doesNotContain(miss1.getId(), miss2.getId());
        assertThat(gd.getPlayerExiledCards(p1)).extracting(Card::getId)
                .contains(miss1.getId(), miss2.getId(), miss3.getId(), miss4.getId(), miss5.getId())
                .doesNotContain(hit1.getId(), hit2.getId(), untouched.getId());
        assertThat(gd.playerDecks.get(p1)).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiles all revealed cards when the chosen name is absent")
    void noMatchExilesAllSeven() {
        UUID p1 = player1.getId();
        List<Card> revealed = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            revealed.add(new KavuScout());
        }
        Card wanted = new KavuTitan();
        List<Card> deck = new ArrayList<>(revealed);
        deck.add(wanted);
        harness.setLibrary(player1, deck);

        cast();
        harness.handleListChoice(player1, "Kavu Titan");

        assertThat(gd.playerHands.get(p1)).extracting(Card::getId).doesNotContain(
                revealed.stream().map(Card::getId).toArray(UUID[]::new));
        assertThat(gd.getPlayerExiledCards(p1)).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(revealed.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(p1)).containsExactly(wanted);
    }

    @Test
    @DisplayName("Reveals only the cards available when the library has fewer than seven cards")
    void smallLibraryRevealsWhatIsAvailable() {
        UUID p1 = player1.getId();
        Card hit = new KavuTitan();
        Card other = new KavuScout();
        harness.setLibrary(player1, List.of(hit, other));

        cast();
        harness.handleListChoice(player1, "Kavu Titan");

        assertThat(gd.playerHands.get(p1)).extracting(Card::getId).contains(hit.getId());
        assertThat(gd.getPlayerExiledCards(p1)).extracting(Card::getId).contains(other.getId());
        assertThat(gd.playerDecks.get(p1)).isEmpty();
    }
}
