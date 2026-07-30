package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SandsOfDeliriumTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills X cards")
    void millsXCards() {
        Permanent sands = addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int before = trimDeck(player2, 10);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(sands.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Milled cards come from the top of the library")
    void millsFromTop() {
        addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        trimDeck(player2, 5);
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card first = deck.get(0);
        Card second = deck.get(1);
        Card third = deck.get(2);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(third);
    }

    @Test
    @DisplayName("X of 0 mills nothing")
    void zeroMillsNothing() {
        addReadySands(player1);
        int before = trimDeck(player2, 10);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controller may target themselves")
    void canTargetSelf() {
        addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int before = trimDeck(player1, 10);

        harness.activateAbility(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(before - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mill is capped by library size")
    void millCappedByLibrarySize() {
        addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        trimDeck(player2, 3);

        harness.activateAbility(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    private Permanent addReadySands(Player player) {
        Permanent perm = new Permanent(new SandsOfDelirium());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
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
