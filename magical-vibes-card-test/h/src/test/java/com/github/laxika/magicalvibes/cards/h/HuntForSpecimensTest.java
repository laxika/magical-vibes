package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntForSpecimens.class, EnvironmentalSciences.class, Forest.class, GrizzlyBears.class, Shock.class})
class HuntForSpecimensTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a black-green Pest token and learns by searching for a Lesson")
    void createsPestAndSearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castHuntForSpecimens();

        Permanent pest = findPermanent(player1, "Pest");
        assertThat(pest.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, pest)).containsExactlyInAnyOrder(
                CardColor.BLACK, CardColor.GREEN);
        assertThat(pest.getCard().getSubtypes()).contains(CardSubtype.PEST);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("Learn can discard a card and draw a card")
    void learnsByDiscardingAndDrawing() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new HuntForSpecimens(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("The Pest token's death trigger makes its controller gain 1 life")
    void pestDeathGainsLife() {
        castHuntForSpecimens();
        Permanent pest = findPermanent(player1, "Pest");
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, pest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(pest.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Learn can decline both discarding and taking an available Lesson")
    void canDeclineLearning() {
        Card kept = new EnvironmentalSciences();
        Card lesson = new EnvironmentalSciences();
        Card libraryCard = new HuntForSpecimens();
        harness.setHand(player1, List.of(new HuntForSpecimens(), kept));
        harness.setLibrary(player1, List.of(libraryCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pest");
    }

    @Test
    @DisplayName("Learn can take a Lesson instead of discarding a card in hand")
    void takesLessonWithoutDiscarding() {
        Card kept = new EnvironmentalSciences();
        Card lesson = new EnvironmentalSciences();
        Card libraryCard = new HuntForSpecimens();
        harness.setHand(player1, List.of(new HuntForSpecimens(), kept));
        harness.setLibrary(player1, List.of(libraryCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(kept, lesson);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(kept);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Learn does not draw when there is no card to discard and no Lesson")
    void emptyHandAndNoLessonStillCreatesPest() {
        Card libraryCard = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(libraryCard));

        castHuntForSpecimens();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pest");
        harness.assertInGraveyard(player1, "Hunt for Specimens");
    }

    private void castHuntForSpecimens() {
        harness.setHand(player1, List.of(new HuntForSpecimens()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
