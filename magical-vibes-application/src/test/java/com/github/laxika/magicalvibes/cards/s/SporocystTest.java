package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sporocyst.class, Forest.class, Island.class, Plains.class})
class SporocystTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X counters and draws at X=5")
    void ravenousAtThreshold() {
        harness.setLibrary(player1, List.of(new Sporocyst(), new Forest(), new Island(), new Plains()));
        castSporocyst(5);

        Permanent sporocyst = findPermanent(player1, "Sporocyst");
        assertThat(sporocyst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);

        resolveLandSearch(5);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanents(player1, "Island")).hasSize(1);
        assertThat(findPermanents(player1, "Plains")).hasSize(1);
        assertThat(findPermanents(player1, "Forest").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Island").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Plains").getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ravenous does not draw below X=5 and Spore Chimney searches up to X basics")
    void ravenousBelowThreshold() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains(), new Sporocyst()));
        castSporocyst(3);

        Permanent sporocyst = findPermanent(player1, "Sporocyst");
        assertThat(sporocyst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        resolveLandSearch(3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                .hasSize(3)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Ravenous and Spore Chimney trigger separately at X=5")
    void drawAndSearchAreSeparateTriggers() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains()));
        harness.setHand(player1, List.of(new Sporocyst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        gs.playCard(gd, player1, 0, 5, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Sporocyst")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Spore Chimney can find fewer than X lands even when more are available")
    void searchCanStopEarly() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains()));
        castSporocyst(3);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(findPermanents(player1, "Island")).isEmpty();
        assertThat(findPermanents(player1, "Plains")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Spore Chimney can find zero lands")
    void searchCanFindNothing() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castSporocyst(1);

        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("At X=0 Sporocyst dies but its search still resolves")
    void zeroXStillResolvesSearch() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new Sporocyst()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sporocyst");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castSporocyst(int x) {
        harness.setHand(player1, List.of(new Sporocyst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x * 2);

        gs.playCard(gd, player1, 0, x, null, null);
        resolveAllTriggers();
    }

    private void resolveLandSearch(int count) {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        if (search == null) {
            harness.passBothPriorities();
            search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        }
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(count);

        for (int i = 0; i < count; i++) {
            if (gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class) == null) {
                break;
            }
            harness.handleCardChosen(player1, 0);
        }
    }
}
