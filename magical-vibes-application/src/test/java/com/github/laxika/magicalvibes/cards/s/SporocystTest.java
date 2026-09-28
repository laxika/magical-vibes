package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Sporocyst.class, Forest.class, Island.class, Plains.class, GrizzlyBears.class})
class SporocystTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X counters and draws at X=5")
    void ravenousAtThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Island(), new Plains()));
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
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains(), new GrizzlyBears()));
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

    private void castSporocyst(int x) {
        harness.setHand(player1, List.of(new Sporocyst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x * 2);

        gs.playCard(gd, player1, 0, x, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
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
