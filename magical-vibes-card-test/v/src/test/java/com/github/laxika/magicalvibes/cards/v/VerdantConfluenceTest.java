package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantConfluence.class, GrizzlyBears.class, HolyDay.class, Forest.class})
class VerdantConfluenceTest extends BaseCardTest {

    @Test
    void repeatedCounterModeCanTargetTheSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(new int[]{0, 0, 0}, List.of(creature.getId(), creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void resolvesCounterReturnAndSearchModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card graveyardPermanent = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setLibrary(player1, List.of(forest));

        cast(new int[]{0, 1, 2}, List.of(creature.getId(), graveyardPermanent.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .singleElement()
                .extracting(Permanent::isTapped)
                .isEqualTo(true);
    }

    @Test
    void repeatedSearchModePutsThreeBasicLandsOntoTheBattlefieldTapped() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        cast(new int[]{2, 2, 2}, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(3)
                .allMatch(Permanent::isTapped);
    }

    @Test
    void cannotReturnNonPermanentCardFromGraveyard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        assertThatThrownBy(() -> cast(new int[]{1, 2, 2}, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repeatedCounterModeCanTargetDifferentPlayersCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0, 0, 0}, List.of(own.getId(), opposing.getId(), own.getId()));
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void repeatedReturnModeReturnsDistinctPermanentCardsIncludingLand() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, third));

        cast(new int[]{1, 1, 1}, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Verdant Confluence");
    }

    @Test
    void repeatedReturnModeCanTargetTheSameCardButReturnsItOnlyOnce() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));

        cast(new int[]{1, 1, 1}, List.of(target.getId(), target.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void cannotReturnPermanentCardFromOpponentsGraveyard() {
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> cast(new int[]{1, 2, 2}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allTargetsBecomingIllegalPreventsUntargetedSearchModes() {
        Card target = new Forest();
        Forest libraryLand = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryLand));

        cast(new int[]{1, 2, 2}, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Verdant Confluence");
    }

    @Test
    void legalCreatureTargetAllowsSearchWhenGraveyardTargetBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card target = new Forest();
        Forest libraryLand = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryLand));

        cast(new int[]{0, 1, 2}, List.of(creature.getId(), target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInHand(player1, "Forest");
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void repeatedSearchCanFailToFindEvenWhenBasicLandIsAvailable() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        cast(new int[]{2, 2, 2}, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Verdant Confluence");
    }

    @Test
    void repeatedSearchWithNoBasicLandsCompletesWithoutFindingCards() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        cast(new int[]{2, 2, 2}, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Verdant Confluence");
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new VerdantConfluence()));
        addMana();
        harness.castModalSorcery(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
