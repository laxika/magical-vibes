package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeaGateLoremasterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each Ally you control")
    void drawsForEachAllyYouControl() {
        Permanent loremaster = addReadyLoremaster(player1);
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SeaGateLoremaster());
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(loremaster.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private Permanent addReadyLoremaster(Player player) {
        Permanent permanent = new Permanent(new SeaGateLoremaster());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
