package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScholarOfNewHorizons.class, Forest.class, Plains.class, GrizzlyBears.class})
class ScholarOfNewHorizonsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent scholar = addScholar();

        assertThat(scholar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes a counter and puts a Plains onto the battlefield tapped when an opponent is ahead")
    void searchesPlainsToBattlefieldTappedWhenOpponentHasMoreLands() {
        Permanent scholar = addScholar();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(scholar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearchDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearchDestinationChoice.class)
                .options()).containsExactly("Hand", "Battlefield tapped");

        harness.handleListChoice(player1, "Battlefield tapped");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .hasSize(1)
                .allMatch(Permanent::isTapped);
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts the revealed Plains into hand when no opponent controls more lands")
    void putsPlainsIntoHandWhenOpponentIsNotAhead() {
        addScholar();
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without a counter to remove")
    void cannotActivateWithoutCounter() {
        Permanent scholar = addScholar();
        scholar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    private Permanent addScholar() {
        return addCreatureReady(player1, new ScholarOfNewHorizons());
    }
}
