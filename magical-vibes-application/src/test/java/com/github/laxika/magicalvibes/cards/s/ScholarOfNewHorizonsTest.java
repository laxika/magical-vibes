package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({ScholarOfNewHorizons.class, Forest.class, Plains.class})
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
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        harness.setHand(player1, List.of());

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
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

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

    @Test
    void mayChooseHandEvenWhenOpponentHasMoreLands() {
        addScholar();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "Hand");

        harness.assertInHand(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canRemoveCounterFromAnotherControlledNoncreaturePermanent() {
        Permanent scholar = addScholar();
        scholar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new Plains()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(forest.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(scholar.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    void mustLetControllerChooseCounterKindBeforePayingCost() {
        Permanent scholar = addScholar();
        scholar.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new Plains()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(scholar.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(scholar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canFailToFindEvenWhenLibraryContainsPlains() {
        addScholar();
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addScholar() {
        Permanent scholar = harness.enterBattlefieldAndReturn(player1, new ScholarOfNewHorizons());
        scholar.setSummoningSick(false);
        return scholar;
    }
}
