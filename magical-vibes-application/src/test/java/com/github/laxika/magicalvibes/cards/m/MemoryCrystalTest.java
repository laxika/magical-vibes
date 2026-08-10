package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.Anoint;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryCrystalTest extends BaseCardTest {

    @Test
    void reducesManaBuybackCost() {
        harness.addToBattlefield(player1, new MemoryCrystal());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstantWithBuyback(player1, 0, targetId);

        assertThat(harness.getGameData().stack.getFirst().isBuyback()).isTrue();
        harness.passBothPriorities();

        assertThat(hand(player1)).anyMatch(Anoint.class::isInstance);
    }

    @Test
    void affectsBuybackCostsOfOpponentsSpells() {
        harness.addToBattlefield(player1, new MemoryCrystal());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Anoint()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstantWithBuyback(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(hand(player2)).anyMatch(Anoint.class::isInstance);
    }

    private List<com.github.laxika.magicalvibes.model.Card> hand(Player player) {
        GameData gameData = harness.getGameData();
        return gameData.playerHands.get(player.getId());
    }
}
