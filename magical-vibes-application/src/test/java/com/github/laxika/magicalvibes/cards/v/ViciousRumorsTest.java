package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViciousRumorsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage, makes each opponent discard and mill, then gains life")
    void resolvesAllEffectsInOrder() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(new ViciousRumors()));
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
