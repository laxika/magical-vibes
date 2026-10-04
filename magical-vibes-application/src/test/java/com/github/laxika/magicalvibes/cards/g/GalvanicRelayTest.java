package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalvanicRelay.class, Shock.class})
class GalvanicRelayTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card but does not allow playing it on the current turn")
    void exilesTopCardAndGrantsPlayPermission() {
        Card top = new Shock();
        Card remaining = new Shock();
        harness.setLibrary(player1, List.of(top, remaining));
        harness.castFromHand(player1, new GalvanicRelay(), "{2}{R}");
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Galvanic Relay")
    void stormCreatesCopyForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new Shock());
        gd.recordSpellCast(player2.getId(), new Shock());
        harness.castFromHand(player1, new GalvanicRelay(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Each Storm copy resolves Galvanic Relay's library effect")
    void stormCopiesExileOneCardEach() {
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        gd.recordSpellCast(player1.getId(), new Shock());
        harness.castFromHand(player1, new GalvanicRelay(), "{2}{R}");
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("The exiled instant cannot be cast during the intervening opponent turn")
    void cannotCastDuringInterveningOpponentTurn() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top, new Shock(), new Shock()));
        harness.castFromHand(player1, new GalvanicRelay(), "{2}{R}");
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("The exiled card can be cast during the controller's next turn by paying its cost")
    void canCastDuringNextTurn() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top, new Shock(), new Shock()));
        harness.castFromHand(player1, new GalvanicRelay(), "{2}{R}");
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, top.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An unplayed card stays exiled and cannot be cast after the next turn ends")
    void permissionExpiresAfterNextTurn() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top, new Shock(), new Shock()));
        harness.castFromHand(player1, new GalvanicRelay(), "{2}{R}");
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("Storm copies resolve safely when the library runs out")
    void stormWithInsufficientLibrary() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        gd.recordSpellCast(player1.getId(), new Shock());
        gd.recordSpellCast(player2.getId(), new Shock());
        harness.castFromHand(player1, new GalvanicRelay(), "{2}{R}");
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
