package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinProdder.class, DevilthornFox.class, Mountain.class})
class SinProdderTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent declines and the revealed card goes to hand")
    void opponentDeclinesPutsCardIntoHand() {
        prepareUpkeep(new DevilthornFox());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player1, "Devilthorn Fox");
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Devilthorn Fox");
    }

    @Test
    @DisplayName("Opponent accepts and is dealt the revealed card's mana value")
    void opponentAcceptsDealsManaValueDamageAndPutsCardInGraveyard() {
        prepareUpkeep(new DevilthornFox());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertNotInHand(player1, "Devilthorn Fox");
    }

    @Test
    @DisplayName("An empty library does not create an opponent choice")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new SinProdder());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent can put a revealed land into the graveyard without taking damage")
    void opponentAcceptsLandWithoutDamage() {
        prepareUpkeep(new Mountain());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotInHand(player1, "Mountain");
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A declined land is put into hand rather than drawn")
    void opponentDeclinesLandPutsItIntoHand() {
        prepareUpkeep(new Mountain());

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Mountain");
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sin Prodder does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SinProdder());
        Card topCard = new DevilthornFox();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInHand(player1, "Devilthorn Fox");
        harness.assertNotInGraveyard(player1, "Devilthorn Fox");
        harness.assertLife(player2, 20);
    }

    private void prepareUpkeep(Card topCard) {
        harness.addToBattlefield(player1, new SinProdder());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
