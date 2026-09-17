package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurveyorsScope.class, Forest.class, GrizzlyBears.class})
class SurveyorsScopeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself and searches for up to one basic land when an opponent is two lands ahead")
    void searchesForBasicLandWhenOpponentIsTwoLandsAhead() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SurveyorsScope);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SurveyorsScope);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .singleElement()
                .matches(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest)
                .singleElement()
                .matches(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Does not search when no opponent is at least two lands ahead")
    void doesNotSearchBelowTheTwoLandThreshold() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SurveyorsScope)
                .noneMatch(p -> p.getCard() instanceof Forest);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SurveyorsScope);
    }
}
