package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TorrentOfLavaTest extends BaseCardTest {

    /** A 2/2 flying creature for test purposes. */
    private static Card flyingCreature() {
        Card card = new Card();
        card.setName("Wind Drake");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}{U}");
        card.setColor(CardColor.BLUE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.FLYING));
        return card;
    }

    @Test
    @DisplayName("Torrent of Lava kills non-flying creatures on both sides")
    void killsNonFlyingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Torrent of Lava does not damage flying creatures")
    void doesNotDamageFlyingCreatures() {
        harness.addToBattlefield(player2, flyingCreature());

        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Torrent of Lava deals no damage to players")
    void dealsNoDamageToPlayers() {
        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Torrent of Lava with X=1 leaves a 2/2 alive")
    void xOneLeavesToughTwoAlive() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TorrentOfLava()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
