package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.cards.s.SkyshroudElf;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodSage.class, TrainedArmodon.class, MoggConscripts.class, Capsize.class, SkyshroudElf.class})
class WoodSageTest extends BaseCardTest {

    private void activate(List<Card> library) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, List.of());
        addCreatureReady(player1, new WoodSage());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to choose a creature card name")
    void promptsForCreatureName() {
        activate(List.of());

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseCreatureNameRevealTopCardsChoice.class);
    }

    @Test
    @DisplayName("Only creature card names are offered")
    void offersOnlyCreatureNames() {
        activate(List.of(new TrainedArmodon(), new Capsize()));

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Trained Armodon").doesNotContain("Capsize");
    }

    @Test
    @DisplayName("A creature card name can be chosen even when no copy is in the game")
    void offersCreatureNamesNotPresentInGame() {
        activate(List.of());

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Trained Armodon");
    }

    @Test
    @DisplayName("All revealed cards with the chosen name go to hand, the rest to the graveyard")
    void namedCardsToHandRestToGraveyard() {
        UUID p1 = player1.getId();
        Card hit1 = new TrainedArmodon();
        Card hit2 = new TrainedArmodon();
        Card miss1 = new MoggConscripts();
        Card miss2 = new Capsize();
        Card untouched = new SkyshroudElf();

        List<Card> deck = new ArrayList<>(List.of(hit1, miss1, hit2, miss2, untouched));

        activate(deck);
        harness.handleListChoice(player1, "Trained Armodon");

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
            chaff.add(new MoggConscripts());
        }
        Card wanted = new TrainedArmodon();
        List<Card> deck = new ArrayList<>(chaff);
        deck.add(wanted);

        activate(deck);
        harness.handleListChoice(player1, "Trained Armodon");

        assertThat(gd.playerGraveyards.get(p1)).extracting(Card::getId)
                .containsAll(chaff.stream().map(Card::getId).toList());
        assertThat(gd.playerHands.get(p1)).extracting(Card::getId).doesNotContain(wanted.getId());
        assertThat(gd.playerDecks.get(p1)).containsExactly(wanted);
    }

    @Test
    @DisplayName("A library with fewer than four cards reveals only what is there")
    void smallLibraryRevealsWhatIsAvailable() {
        UUID p1 = player1.getId();
        Card hit = new TrainedArmodon();
        Card other = new MoggConscripts();

        activate(List.of(hit, other));
        harness.handleListChoice(player1, "Trained Armodon");

        assertThat(gd.playerHands.get(p1)).extracting(Card::getId).contains(hit.getId());
        assertThat(gd.playerGraveyards.get(p1)).extracting(Card::getId).contains(other.getId());
        assertThat(gd.playerDecks.get(p1)).isEmpty();
    }

    @Test
    @DisplayName("An empty library finishes resolving without moving cards")
    void emptyLibraryFinishesResolving() {
        activate(List.of());
        List<Card> graveyardBefore = List.copyOf(gd.playerGraveyards.get(player1.getId()));

        harness.handleListChoice(player1, "Trained Armodon");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cards stay in the library until the creature name is chosen")
    void choosesNameBeforeRevealingCards() {
        Card hit = new TrainedArmodon();
        Card miss = new Capsize();
        activate(List.of(hit, miss));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit, miss);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("reveals Trained Armodon")).isFalse();

        harness.handleListChoice(player1, "Trained Armodon");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("reveals Trained Armodon, Capsize")).isTrue();
    }

    @Test
    @DisplayName("Every copy goes to hand when all four revealed cards match")
    void allFourMatchesGoToHand() {
        List<Card> hits = List.of(new TrainedArmodon(), new TrainedArmodon(),
                new TrainedArmodon(), new TrainedArmodon());
        activate(hits);
        List<Card> graveyardBefore = List.copyOf(gd.playerGraveyards.get(player1.getId()));

        harness.handleListChoice(player1, "Trained Armodon");

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(hits);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A noncreature card name cannot be submitted for Wood Sage")
    void rejectsNoncreatureCardName() {
        Card instant = new Capsize();
        activate(List.of(instant));

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Capsize"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleListChoice(player1, "Trained Armodon");
        harness.assertInGraveyard(player1, "Capsize");
    }
}
