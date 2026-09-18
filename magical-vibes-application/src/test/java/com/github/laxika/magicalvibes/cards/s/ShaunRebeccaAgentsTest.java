package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ShaunRebeccaAgents.class)
class ShaunRebeccaAgentsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts The Animus onto the battlefield from the graveyard")
    void searchesGraveyardForTheAnimus() {
        Card animus = animusCard();
        harness.setGraveyard(player1, List.of(animus));
        harness.setHand(player1, List.of(new ShaunRebeccaAgents()));

        castShaunRebecca();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Animus");
        harness.assertNotInGraveyard(player1, "The Animus");
    }

    @Test
    @DisplayName("Searches the library for The Animus")
    void searchesLibraryForTheAnimus() {
        Card animus = animusCard();
        harness.setLibrary(player1, List.of(animus));
        harness.setHand(player1, List.of(new ShaunRebeccaAgents()));

        castShaunRebecca();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "The Animus");
    }

    @Test
    @DisplayName("Adds colorless mana and mills two cards")
    void addsManaAndMillsTwoCards() {
        Permanent shaunRebecca = addCreatureReady(player1, new ShaunRebeccaAgents());
        Card firstMilled = new Card();
        Card secondMilled = new Card();
        harness.setLibrary(player1, List.of(firstMilled, secondMilled));
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(shaunRebecca.isTapped()).isTrue();
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstMilled, secondMilled);
    }

    private void castShaunRebecca() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private Card animusCard() {
        Card animus = new Card();
        animus.setName("The Animus");
        animus.setType(CardType.ARTIFACT);
        animus.setManaCost("{3}");
        return animus;
    }
}
