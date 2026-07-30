package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerfolkMesmeristTest extends BaseCardTest {

    @Test
    @DisplayName("Ability mills two cards from target player's library")
    void millsTwoCards() {
        Permanent mesmerist = addReadyMesmerist(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int deckSizeBefore = trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(mesmerist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Milled cards come from the top of the library")
    void millsFromTop() {
        addReadyMesmerist(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 5) {
            deck.removeFirst();
        }
        Card first = deck.get(0);
        Card second = deck.get(1);
        Card third = deck.get(2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(third);
    }

    @Test
    @DisplayName("Controller can target themselves")
    void canTargetSelf() {
        addReadyMesmerist(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int deckSizeBefore = trimDeck(player1, 10);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mill is capped by remaining library size")
    void millCappedByLibrarySize() {
        addReadyMesmerist(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        trimDeck(player2, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutMana() {
        addReadyMesmerist(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent mesmerist = harness.addToBattlefieldAndReturn(player1, new MerfolkMesmerist());
        mesmerist.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMesmerist(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MerfolkMesmerist());
        perm.setSummoningSick(false);
        return perm;
    }

    private int trimDeck(Player player, int size) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        while (deck.size() > size) {
            deck.removeFirst();
        }
        return deck.size();
    }
}
