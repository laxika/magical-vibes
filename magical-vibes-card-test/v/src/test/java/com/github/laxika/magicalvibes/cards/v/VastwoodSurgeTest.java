package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VastwoodSurge.class, Forest.class, Island.class, CliffhavenSellSword.class})
class VastwoodSurgeTest extends BaseCardTest {

    @Test
    void withoutKickerSearchesForUpToTwoBasicLandsTapped() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new VastwoodSurge()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new CliffhavenSellSword()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void kickedPutsTwoCountersOnEachCreatureYouControl() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new VastwoodSurge()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new CliffhavenSellSword()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castKickedSorcery(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void kickedStillAddsCountersWhenChoosingZeroLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new VastwoodSurge()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castKickedSorcery(player1, 0, null);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleCardChosen(player1, -1);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vastwood Surge");
    }

    @Test
    void kickedCanStopAfterFindingOneLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new VastwoodSurge()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castKickedSorcery(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(Permanent::isTapped)
                .allMatch(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 0)
                .hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vastwood Surge");
    }

    @Test
    void kickedAddsCountersWithAnEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new VastwoodSurge()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castKickedSorcery(player1, 0, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vastwood Surge");
    }

    @Test
    void kickedAddsCountersWhenLibraryContainsNoBasicLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new VastwoodSurge()));
        harness.setLibrary(player1, List.of(new CliffhavenSellSword()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castKickedSorcery(player1, 0, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vastwood Surge");
    }
}
