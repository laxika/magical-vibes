package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoorkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Mills one card when Doorkeeper is the only creature with defender")
    void millsOneForItself() {
        addReadyDoorkeeper(player1);
        int deckSizeBefore = trimDeck(player2);

        activate(player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mill amount counts every creature you control with defender")
    void millsPerDefender() {
        addReadyDoorkeeper(player1);
        addPermanent(player1, new WallOfAir());
        addPermanent(player1, new GrizzlyBears());
        int deckSizeBefore = trimDeck(player2);

        activate(player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Defenders controlled by the opponent are not counted")
    void ignoresOpponentDefenders() {
        addReadyDoorkeeper(player1);
        addPermanent(player2, new WallOfAir());
        int deckSizeBefore = trimDeck(player2);

        activate(player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Can target yourself with the mill ability")
    void canTargetSelf() {
        addReadyDoorkeeper(player1);
        int deckSizeBefore = trimDeck(player1);

        activate(player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void activate(java.util.UUID targetId) {
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }

    private int trimDeck(Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        while (deck.size() > 5) {
            deck.removeFirst();
        }
        return deck.size();
    }

    private Permanent addReadyDoorkeeper(Player player) {
        return addPermanent(player, new Doorkeeper());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
