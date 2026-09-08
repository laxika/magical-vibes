package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolgariBrownscale.class, Forest.class})
class GolgariBrownscaleTest extends BaseCardTest {

    @Test
    @DisplayName("Dredging it into hand gains 2 life")
    void dredgingIntoHandGainsLife() {
        GolgariBrownscale brownscale = new GolgariBrownscale();
        List<Card> milled = List.of(new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(brownscale));
        harness.setLibrary(player1, milled);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(brownscale);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Declining dredge does not gain life")
    void decliningDredgeDoesNotGainLife() {
        GolgariBrownscale brownscale = new GolgariBrownscale();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(brownscale));
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(brownscale);
        harness.assertLife(player1, 20);
    }
}
