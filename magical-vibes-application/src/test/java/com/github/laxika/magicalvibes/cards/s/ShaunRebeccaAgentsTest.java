package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.t.TheAnimus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShaunRebeccaAgents.class, TheAnimus.class})
class ShaunRebeccaAgentsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts The Animus onto the battlefield from the graveyard")
    void searchesGraveyardForTheAnimus() {
        Card animus = new TheAnimus();
        harness.setGraveyard(player1, List.of(animus));

        castShaunRebecca();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Animus");
        harness.assertNotInGraveyard(player1, "The Animus");
    }

    @Test
    @DisplayName("Searches the library for The Animus")
    void searchesLibraryForTheAnimus() {
        Card animus = new TheAnimus();
        harness.setLibrary(player1, List.of(animus));

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
        Card firstMilled = new TheAnimus();
        Card secondMilled = new ShaunRebeccaAgents();
        harness.setLibrary(player1, List.of(firstMilled, secondMilled));
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(shaunRebecca.isTapped()).isTrue();
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstMilled, secondMilled);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstMilled, secondMilled);
    }

    @Test
    @DisplayName("Puts The Animus onto the battlefield from the hand")
    void searchesHandForTheAnimus() {
        harness.setHand(player1, List.of(new ShaunRebeccaAgents(), new TheAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Animus");
        harness.assertNotInHand(player1, "The Animus");
    }

    @Test
    @DisplayName("Mills only the available card when the library has one card")
    void millsShortLibrary() {
        addCreatureReady(player1, new ShaunRebeccaAgents());
        Card milled = new TheAnimus();
        harness.setLibrary(player1, List.of(milled));
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can choose the library copy of The Animus instead of the graveyard copy")
    void choosesLibraryCopyOverGraveyardCopy() {
        Card graveyardAnimus = new TheAnimus();
        Card libraryAnimus = new TheAnimus();
        harness.setGraveyard(player1, List.of(graveyardAnimus));
        harness.setLibrary(player1, List.of(libraryAnimus));

        castShaunRebecca();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardAnimus);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(libraryAnimus));
    }
    private void castShaunRebecca() {
        harness.castFromHand(player1, new ShaunRebeccaAgents(), "{1}{G}{W}{U}");
    }
}
