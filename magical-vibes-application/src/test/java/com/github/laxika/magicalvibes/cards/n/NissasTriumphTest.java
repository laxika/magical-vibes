package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({NissasTriumph.class, Forest.class, GhostQuarter.class, GrizzlyBears.class,
        Island.class, NissaGenesisMage.class, Plains.class})
class NissasTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Nissa, offers up to two basic Forest cards")
    void withoutNissaOffersBasicForests() {
        setLibrary(new Forest(), new Island(), new GhostQuarter(), new GrizzlyBears());

        castTriumph();

        PendingInteraction.LibrarySearch search = librarySearch();
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("With a Nissa, offers up to three land cards")
    void withNissaOffersThreeLands() {
        Permanent nissa = harness.addToBattlefieldAndReturn(player1, new NissaGenesisMage());
        nissa.setCounterCount(CounterType.LOYALTY, 5);
        setLibrary(new Forest(), new Island(), new GhostQuarter(), new GrizzlyBears(), new Plains());

        castTriumph();

        PendingInteraction.LibrarySearch search = librarySearch();
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island", "Ghost Quarter", "Plains");
    }

    private void castTriumph() {
        harness.setHand(player1, List.of(new NissasTriumph()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private PendingInteraction.LibrarySearch librarySearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
