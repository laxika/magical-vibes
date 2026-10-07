package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThaumaturgesFamiliar.class})
class ThaumaturgesFamiliarTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldOffersScryOne() {
        castFamiliar();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();

        resolveFamiliar();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    void scryOneCanKeepTheTopCard() {
        Card topCard = new ThaumaturgesFamiliar();
        harness.setLibrary(player1, List.of(topCard, new ThaumaturgesFamiliar()));
        castFamiliar();
        resolveFamiliar();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void scryOneCanPutTheTopCardOnTheBottom() {
        Card topCard = new ThaumaturgesFamiliar();
        Card bottomCard = new ThaumaturgesFamiliar();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        castFamiliar();
        resolveFamiliar();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
    }

    @Test
    void scryWithAnEmptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        castFamiliar();
        resolveFamiliar();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Thaumaturge's Familiar");
    }

    private void castFamiliar() {
        harness.castFromHand(player1, new ThaumaturgesFamiliar(), "{3}");
    }

    private void resolveFamiliar() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
