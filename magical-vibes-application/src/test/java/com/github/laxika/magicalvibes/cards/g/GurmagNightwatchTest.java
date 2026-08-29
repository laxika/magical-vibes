package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GurmagNightwatch.class, Forest.class, Shock.class})
class GurmagNightwatchTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers the top three cards and keeps the chosen card on top")
    void etbKeepsChosenCardOnTopAndGravesTheRest() {
        Card chosen = new Forest();
        Card restOne = new Shock();
        Card restTwo = new Shock();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        castGurmagNightwatch();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
    }

    @Test
    @DisplayName("Declining the ETB choice puts all three cards into the graveyard")
    void decliningGravesAllLookedAtCards() {
        Card first = new Forest();
        Card second = new Shock();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));

        castGurmagNightwatch();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);
    }

    private void castGurmagNightwatch() {
        harness.setHand(player1, List.of(new GurmagNightwatch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }
}
