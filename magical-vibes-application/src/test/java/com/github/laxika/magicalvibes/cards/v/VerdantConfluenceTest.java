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

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new VerdantConfluence()));
        addMana();
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                null, null, targetIds, List.of());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
