package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EtaliPrimalStormTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card of each library and offers every exiled spell")
    void exilesTopCardOfEachLibraryAndOffersSpells() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Forest player1Remainder = new Forest();
        Forest player2Remainder = new Forest();
        harness.setLibrary(player1, List.of(shock, player1Remainder));
        harness.setLibrary(player2, List.of(bears, player2Remainder));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactlyInAnyOrder(shock.getId(), bears.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(shock, bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Remainder);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Remainder);
    }

    @Test
    @DisplayName("Casts an opponent-owned exiled spell without paying and leaves unchosen cards exiled")
    void castsOpponentOwnedSpellWithoutPaying() {
        Forest land = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(bears));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).anyMatch(stackEntry -> stackEntry.getCard() == bears
                && stackEntry.getControllerId().equals(player1.getId()));
        assertThat(gd.stack.stream().map(stackEntry -> stackEntry.getCard().getId()))
                .doesNotContain(land.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).contains(land);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).doesNotContain(bears);
    }
}
