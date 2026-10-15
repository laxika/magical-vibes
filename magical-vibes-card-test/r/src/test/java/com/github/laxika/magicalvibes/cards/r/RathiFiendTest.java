package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PhyrexianProwler;
import com.github.laxika.magicalvibes.cards.s.SkyshroudRidgeback;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RathiFiend.class, RathiIntimidator.class, SkyshroudRidgeback.class, PhyrexianProwler.class})
class RathiFiendTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player lose 3 life")
    void etbMakesEachPlayerLoseThreeLife() {
        harness.setHand(player1, List.of(new RathiFiend()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Activated ability puts a qualifying Mercenary permanent onto the battlefield")
    void searchesMercenaryPermanentWithManaValueAtMostThree() {
        var fiend = addCreatureReady(player1, new RathiFiend());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new RathiIntimidator(), new SkyshroudRidgeback(), new PhyrexianProwler()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Rathi Intimidator");

        harness.handleCardChosen(player1, 0);

        assertThat(fiend.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Rathi Intimidator");
        harness.assertNotOnBattlefield(player1, "Skyshroud Ridgeback");
        harness.assertNotOnBattlefield(player1, "Phyrexian Prowler");
    }

    @Test
    @DisplayName("A matching Mercenary may be left in the library and the library is still shuffled")
    void mayFailToFindMatchingMercenary() {
        var fiend = addCreatureReady(player1, new RathiFiend());
        var intimidator = new RathiIntimidator();
        harness.setLibrary(player1, List.of(intimidator));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(fiend.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(intimidator);
        harness.assertNotOnBattlefield(player1, "Rathi Intimidator");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The activated search resolves after its source leaves the battlefield")
    void searchResolvesWithoutSource() {
        var fiend = addCreatureReady(player1, new RathiFiend());
        var intimidator = new RathiIntimidator();
        var opposingIntimidator = new RathiIntimidator();
        harness.setLibrary(player1, List.of(intimidator));
        harness.setLibrary(player2, List.of(opposingIntimidator));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(fiend);
        gd.playerGraveyards.get(player1.getId()).add(fiend.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Rathi Intimidator");
        harness.assertNotOnBattlefield(player1, "Rathi Fiend");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingIntimidator);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
