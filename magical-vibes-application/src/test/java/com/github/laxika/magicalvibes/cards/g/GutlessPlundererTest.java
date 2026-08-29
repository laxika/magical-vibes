package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GutlessPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("Raid offers the top three cards and keeps the chosen card on top")
    void raidKeepsChosenCardOnTopAndGravesTheRest() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card chosen = new Forest();
        Card restOne = new Shock();
        Card restTwo = new Shock();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
    }

    @Test
    @DisplayName("Declining the raid choice puts all three cards into the graveyard")
    void decliningGravesAllLookedAtCards() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card first = new Forest();
        Card second = new Shock();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);
    }

    @Test
    @DisplayName("Raid does not trigger when no creature was attacked with this turn")
    void noRaidDoesNothing() {
        List<Card> library = List.of(new Forest(), new Shock(), new Shock());
        harness.setLibrary(player1, library);

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPlunderer() {
        harness.setHand(player1, List.of(new GutlessPlunderer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
