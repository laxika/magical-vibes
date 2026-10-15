package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouHappenOnAGlade.class, Forest.class, Plains.class, GrizzlyBears.class})
class YouHappenOnAGladeTest extends BaseCardTest {

    @Test
    void journeyOnSearchesForUpToTwoBasicLands() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        GrizzlyBears nonBasicLand = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, plains, nonBasicLand));
        prepareSpell();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, plains);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasicLand);
    }

    @Test
    void makeCampReturnsOnlyAPermanentCardFromGraveyard() {
        Card permanent = new GrizzlyBears();
        Card instant = new YouHappenOnAGlade();
        harness.setGraveyard(player1, List.of(permanent, instant));
        prepareSpell();

        harness.castModalInstant(player1, 0, 1, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(permanent.getId());

        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(permanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(permanent);
    }

    @Test
    void journeyOnCanFindZeroLandsEvenWhenLandsAreAvailable() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));
        prepareSpell();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, plains);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void journeyOnCanStopAfterFindingOneLand() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));
        prepareSpell();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest).doesNotContain(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void makeCampCannotBeCastWithoutAPermanentInYourGraveyard() {
        harness.setGraveyard(player1, List.of(new YouHappenOnAGlade()));
        harness.setGraveyard(player2, List.of(new Forest()));
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void makeCampRequiresChoosingOneTarget() {
        harness.setGraveyard(player1, List.of(new Forest()));
        prepareSpell();
        harness.castModalInstant(player1, 0, 1, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void makeCampReturnsALandCard() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        prepareSpell();
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    void makeCampCannotReturnATargetThatLeftTheGraveyard() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        prepareSpell();
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YouHappenOnAGlade()));
        harness.addMana(player1, ManaColor.GREEN, 3);
    }
}
