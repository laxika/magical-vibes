package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldThrush.class, Forest.class, GrizzlyBears.class})
class OldThrushTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 2 life and offers a basic land search")
    void gainsLifeAndOffersBasicLandSearch() {
        Card basicLand = new Forest();
        Card nonland = new GrizzlyBears();
        setup(List.of(basicLand, nonland));

        resolveEtbMayPrompt();

        harness.assertLife(player1, 22);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(basicLand);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search still gains 2 life and leaves the library unchanged")
    void decliningSearchStillGainsLife() {
        Card basicLand = new Forest();
        Card nonland = new GrizzlyBears();
        setup(List.of(basicLand, nonland));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 22);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand, nonland);
    }

    private void setup(List<Card> library) {
        harness.setHand(player1, List.of(new OldThrush()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(library);
        harness.setLife(player1, 20);
        harness.castCreature(player1, 0);
    }

    private void resolveEtbMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
