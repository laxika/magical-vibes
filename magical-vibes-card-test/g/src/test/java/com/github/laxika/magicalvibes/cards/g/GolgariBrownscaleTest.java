package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolgariBrownscale.class, Forest.class, Recollect.class})
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
    @DisplayName("Dredge is not offered when the library has fewer than two cards")
    void cannotDredgeWithTooFewLibraryCards() {
        GolgariBrownscale brownscale = new GolgariBrownscale();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(brownscale));
        harness.setLibrary(player1, List.of(topCard));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(brownscale);
        harness.assertLife(player1, 20);
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

    @Test
    @DisplayName("Returning it with Recollect creates a separate life gain trigger")
    void returningWithoutDredgeGainsLifeWhenTriggerResolves() {
        GolgariBrownscale brownscale = new GolgariBrownscale();
        harness.setGraveyard(player1, List.of(brownscale));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, brownscale.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(brownscale);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Milling another Brownscale during dredge does not trigger its life gain")
    void millingAnotherBrownscaleDoesNotGainExtraLife() {
        GolgariBrownscale returned = new GolgariBrownscale();
        GolgariBrownscale milled = new GolgariBrownscale();
        Card forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(returned));
        harness.setLibrary(player1, List.of(milled, forest));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled, forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Dredging the other player's Brownscale gains life only for that player")
    void dredgeGainsLifeForGraveyardOwner() {
        GolgariBrownscale brownscale = new GolgariBrownscale();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(brownscale));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.handleGraveyardCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(brownscale);
        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);
    }
}
