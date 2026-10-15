package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Weftwalking.class, GrizzlyBears.class, Shock.class, Commandeer.class})
class WeftwalkingTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, shuffles its controller's hand and graveyard into the library and draws seven")
    void castResetsControllerHandAndGraveyard() {
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new Weftwalking(), handCard));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).contains(handCard)
                || gd.playerDecks.get(player1.getId()).contains(handCard)).isTrue();
        assertThat(gd.playerHands.get(player1.getId()).contains(graveyardCard)
                || gd.playerDecks.get(player1.getId()).contains(graveyardCard)).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The first spell of the active player's turn may be cast for free, once")
    void firstSpellOfActivePlayersTurnIsFreeOnce() {
        harness.addToBattlefield(player1, new Weftwalking());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The free spell permission applies to each player only during their own turn")
    void freeSpellPermissionIsForTheActivePlayer() {
        harness.addToBattlefield(player1, new Weftwalking());
        harness.setHand(player2, List.of(new Shock()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Putting Weftwalking onto the battlefield without casting it does not reset the hand")
    void enteringWithoutBeingCastDoesNotResetHand() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, deckOf(20));

        harness.enterBattlefieldAndReturn(player1, new Weftwalking());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
    }

    @Test
    @DisplayName("An earlier paid spell prevents a free spell when Weftwalking enters later that turn")
    void earlierSpellStillCountsWhenWeftwalkingEnters() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new Weftwalking());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Multiple Weftwalkings do not allow multiple free spells in one turn")
    void multipleCopiesOnlyAllowTheFirstSpellForFree() {
        harness.addToBattlefield(player1, new Weftwalking());
        harness.addToBattlefield(player2, new Weftwalking());
        harness.setHand(player1, List.of(new Shock(), new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Casting Weftwalking itself uses up the first spell of the turn")
    void castingWeftwalkingDoesNotMakeTheNextSpellFree() {
        harness.setHand(player1, List.of(new Weftwalking()));
        harness.setLibrary(player1, deckOf(20));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Each new turn permits its active player's first spell for free")
    void freeFirstSpellIsAvailableAgainOnANewTurn() {
        harness.addToBattlefield(player1, new Weftwalking());
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Taking control of Weftwalking on the stack does not trigger its hand reset")
    void spellControllerWhoDidNotCastItDoesNotResetTheirHand() {
        Weftwalking weftwalking = new Weftwalking();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(weftwalking));
        harness.setHand(player2, List.of(new Commandeer(), handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player2, deckOf(20));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, weftwalking.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Weftwalking");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCard)
                .hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(20);
    }

    private List<Card> deckOf(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        return deck;
    }
}
