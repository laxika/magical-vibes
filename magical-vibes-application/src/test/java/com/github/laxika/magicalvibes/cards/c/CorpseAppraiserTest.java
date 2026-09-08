package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseAppraiser.class, Forest.class, GrizzlyBears.class, Shock.class})
class CorpseAppraiserTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a creature card and looks at the top three cards")
    void exilesCreatureAndChoosesFromTopThree() {
        Card target = new GrizzlyBears();
        Card kept = new Forest();
        Card graveyardOne = new Shock();
        Card graveyardTwo = new Shock();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(kept, graveyardOne, graveyardTwo));

        castCorpseAppraiser();

        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardOne, graveyardTwo);
    }

    @Test
    @DisplayName("Choosing no graveyard target skips the library effect")
    void noGraveyardTargetSkipsLibraryEffect() {
        Card target = new GrizzlyBears();
        Card topCard = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(topCard));

        castCorpseAppraiser();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("ETB does not offer noncreature graveyard cards")
    void noncreatureIsNotTargetable() {
        Card target = new Shock();
        harness.setGraveyard(player2, List.of(target));

        castCorpseAppraiser();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    private void castCorpseAppraiser() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CorpseAppraiser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
