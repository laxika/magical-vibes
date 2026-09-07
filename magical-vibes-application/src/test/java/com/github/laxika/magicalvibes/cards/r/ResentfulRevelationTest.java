package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResentfulRevelation.class, GrizzlyBears.class, Shock.class})
class ResentfulRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into hand and the rest into the graveyard")
    void choosesOneCardAndPutsRestInGraveyard() {
        Card chosen = new GrizzlyBears();
        Card restOne = new Shock();
        Card restTwo = new Shock();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        castResentfulRevelation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback resolves the spell and exiles it")
    void flashbackResolvesAndExiles() {
        Card flashbackCard = new ResentfulRevelation();
        Card chosen = new GrizzlyBears();
        Card restOne = new Shock();
        Card restTwo = new Shock();
        harness.setGraveyard(player1, List.of(flashbackCard));
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(restOne, restTwo);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(flashbackCard);
    }

    private void castResentfulRevelation() {
        harness.setHand(player1, List.of(new ResentfulRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
