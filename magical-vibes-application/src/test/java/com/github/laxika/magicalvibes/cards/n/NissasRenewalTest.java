package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissasRenewal.class, Forest.class, Island.class, Mountain.class, GrizzlyBears.class})
class NissasRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to three basic lands and puts them onto the battlefield tapped")
    void searchesForThreeBasicLandsTapped() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(3)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Gains 7 life after the library search")
    void gainsSevenLifeAfterSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new NissasRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
    }
}
