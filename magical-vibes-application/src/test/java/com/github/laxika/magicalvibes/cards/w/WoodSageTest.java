package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WoodSageTest extends BaseCardTest {

    private void activate() {
        addCreatureReady(player1, new WoodSage());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to choose a creature card name")
    void promptsForCreatureName() {
        activate();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseCreatureNameRevealTopCardsChoice.class);
    }

    @Test
    @DisplayName("Only creature card names are offered")
    void offersOnlyCreatureNames() {
        UUID p1 = player1.getId();
        gd.playerDecks.get(p1).addFirst(instant("Shock Bolt"));
        gd.playerDecks.get(p1).addFirst(creature("Wall of Wood"));

        activate();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Wall of Wood").doesNotContain("Shock Bolt");
    }

    @Test
    @DisplayName("All revealed cards with the chosen name go to hand, the rest to the graveyard")
    void namedCardsToHandRestToGraveyard() {
        UUID p1 = player1.getId();
        Card hit1 = creature("Llanowar Elf");
        Card hit2 = creature("Llanowar Elf");
        Card miss1 = creature("Some Bear");
        Card miss2 = instant("Some Bolt");
        Card untouched = creature("Deep Card");

        List<Card> deck = new ArrayList<>(List.of(hit1, miss1, hit2, miss2, untouched));
        gd.playerDecks.put(p1, deck);

        activate();
        harness.handleListChoice(player1, "Llanowar Elf");

        assertThat(gd.playerHands.get(p1)).extracting(Card::getId)
                .contains(hit1.getId(), hit2.getId())
                .doesNotContain(miss1.getId(), miss2.getId());
        assertThat(gd.playerGraveyards.get(p1)).extracting(Card::getId)
                .contains(miss1.getId(), miss2.getId());
        assertThat(gd.playerDecks.get(p1)).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no match, all four revealed cards go to the graveyard")
    void noMatchBinsAllFour() {
        UUID p1 = player1.getId();
        List<Card> chaff = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            chaff.add(creature("Chaff " + i));
        }
        Card wanted = creature("Wanted Beast");
        List<Card> deck = new ArrayList<>(chaff);
        deck.add(wanted);
        gd.playerDecks.put(p1, deck);

        activate();
        harness.handleListChoice(player1, "Wanted Beast");

        assertThat(gd.playerGraveyards.get(p1)).extracting(Card::getId)
                .containsAll(chaff.stream().map(Card::getId).toList());
        assertThat(gd.playerHands.get(p1)).extracting(Card::getId).doesNotContain(wanted.getId());
        assertThat(gd.playerDecks.get(p1)).containsExactly(wanted);
    }

    @Test
    @DisplayName("A library with fewer than four cards reveals only what is there")
    void smallLibraryRevealsWhatIsAvailable() {
        UUID p1 = player1.getId();
        Card hit = creature("Tiny Elf");
        Card other = creature("Other Elf");
        gd.playerDecks.put(p1, new ArrayList<>(List.of(hit, other)));

        activate();
        harness.handleListChoice(player1, "Tiny Elf");

        assertThat(gd.playerHands.get(p1)).extracting(Card::getId).contains(hit.getId());
        assertThat(gd.playerGraveyards.get(p1)).extracting(Card::getId).contains(other.getId());
        assertThat(gd.playerDecks.get(p1)).isEmpty();
    }

    private static Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        return card;
    }

    private static Card instant(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        return card;
    }
}
