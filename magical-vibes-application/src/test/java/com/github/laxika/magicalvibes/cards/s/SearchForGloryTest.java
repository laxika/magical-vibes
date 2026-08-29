package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HalvarGodOfBattle;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchForGloryTest extends BaseCardTest {

    @Test
    void searchesForSnowPermanentsLegendariesAndSagas() {
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of(
                new SnowCoveredPlains(), new HalvarGodOfBattle(), new HistoryOfBenalia(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Snow-Covered Plains", "Halvar, God of Battle", "History of Benalia");

        int halvarIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Halvar, God of Battle");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(halvarIndex));

        harness.assertInHand(player1, "Halvar, God of Battle");
    }

    @Test
    void gainsLifeForManaSpentFromSnowSource() {
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        harness.tapPermanent(player1, 0);
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setLife(player1, 10);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }

    @Test
    void doesNotGainLifeForManaNotFromSnowSource() {
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setLife(player1, 10);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }
}
