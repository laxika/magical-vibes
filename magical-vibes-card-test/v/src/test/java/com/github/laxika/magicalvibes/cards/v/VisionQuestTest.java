package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, Ornithopter.class, VisionQuest.class, VivVisionTeenSynthezoid.class})
class VisionQuestTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the graveyard and puts the artifact creature onto the battlefield without haste at X=0")
    void searchesGraveyardWithoutHasteAtZero() {
        Card ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(ornithopter));
        castVisionQuest(0);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.pool()).containsExactly(ornithopter);

        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));

        Permanent found = findPermanent(player1, "Ornithopter");
        assertThat(found.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(found.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Searches the library, adds X counters, and grants haste when X is at least four")
    void searchesLibraryWithCountersAndHasteAtFour() {
        Card ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of(ornithopter, new GrizzlyBears()));
        harness.setGraveyard(player1, List.of());
        castVisionQuest(4);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.pool()).containsExactly(ornithopter);

        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));

        Permanent found = findPermanent(player1, "Ornithopter");
        assertThat(found.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(found.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Finding no card must not grant haste to an existing creature")
    void noMatchDoesNotGrantHaste() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());

        castVisionQuest(4);

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Failing to find a library card must not grant haste to an existing creature")
    void decliningSearchDoesNotGrantHaste() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.setGraveyard(player1, List.of());

        castVisionQuest(4);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An artifact creature with mana value exactly X is eligible and has no haste at X=3")
    void includesManaValueEqualToX() {
        Card viv = new VivVisionTeenSynthezoid();
        harness.setLibrary(player1, List.of(viv));
        harness.setGraveyard(player1, List.of());

        castVisionQuest(3);
        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.pool()).containsExactly(viv);
        harness.handleMultipleCardsChosen(player1, List.of(viv.getId()));

        Permanent found = findPermanent(player1, "Viv Vision, Teen Synthezoid");
        assertThat(found.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(found.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertNotInGraveyard(player1, "Viv Vision, Teen Synthezoid");
    }

    @Test
    @DisplayName("Artifact creatures above X are excluded from both search zones")
    void excludesManaValueAboveX() {
        Card ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of(new VivVisionTeenSynthezoid(), ornithopter));
        harness.setGraveyard(player1, List.of(new VivVisionTeenSynthezoid()));

        castVisionQuest(2);
        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.pool()).containsExactly(ornithopter);
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));

        assertThat(findPermanent(player1, "Ornithopter").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Viv Vision, Teen Synthezoid");
        harness.assertInGraveyard(player1, "Viv Vision, Teen Synthezoid");
    }

    @Test
    @DisplayName("Haste is granted only to the creature found, even when another creature is controlled")
    void grantsHasteOnlyToFoundCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(ornithopter));

        castVisionQuest(4);
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));

        assertThat(findPermanent(player1, "Ornithopter").hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertNotInGraveyard(player1, "Ornithopter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(findPermanent(player1, "Ornithopter").hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(findPermanent(player1, "Ornithopter").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
    }

    private void castVisionQuest(int xValue) {
        harness.setHand(player1, List.of(new VisionQuest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }
}
