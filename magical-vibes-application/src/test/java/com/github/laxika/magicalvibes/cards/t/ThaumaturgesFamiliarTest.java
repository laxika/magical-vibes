package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThaumaturgesFamiliar.class, GrizzlyBears.class})
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
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));
        castFamiliar();
        resolveFamiliar();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void scryOneCanPutTheTopCardOnTheBottom() {
        Card topCard = new GrizzlyBears();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        castFamiliar();
        resolveFamiliar();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
    }

    private void castFamiliar() {
        harness.setHand(player1, List.of(new ThaumaturgesFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveFamiliar() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
