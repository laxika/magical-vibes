package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wildfire.class, Forest.class, Mountain.class, ShivanHellkite.class, ThunderingGiant.class})
class WildfireTest extends BaseCardTest {

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Mountain());
            harness.addToBattlefield(player2, new Forest());
        }
    }

    private void castWildfire() {
        harness.castFromHand(player1, new Wildfire(), "{4}{R}{R}");
        harness.passBothPriorities();
    }

    private long landCount(Player player) {
        return countPermanents(player, "Mountain")
                + countPermanents(player, "Forest");
    }

    @Test
    @DisplayName("Each player with exactly four lands sacrifices all of them")
    void eachPlayerSacrificesFourLands() {
        addLands(4);

        castWildfire();

        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isZero();
    }

    @Test
    @DisplayName("A player with fewer than four lands sacrifices all of them")
    void playerWithFewerLandsSacrificesAll() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        castWildfire();

        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isZero();
    }

    @Test
    @DisplayName("A player with more than four lands chooses which four to sacrifice")
    void playerWithMoreLandsChooses() {
        // player1 has exactly 4 (auto), player2 has 5 (must choose 4)
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.addToBattlefield(player2, new Mountain());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        castWildfire();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        List<UUID> toSacrifice = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .map(Permanent::getId)
                .toList();
        harness.handleMultiplePermanentsChosen(player2, toSacrifice);

        assertThat(landCount(player2)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(landCount(player1)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A player with no lands still has their creatures dealt damage")
    void playerWithNoLandsStillHasCreaturesDamaged() {
        harness.addToBattlefield(player1, new ShivanHellkite());
        harness.addToBattlefield(player2, new ThunderingGiant());

        castWildfire();

        harness.assertOnBattlefield(player1, "Shivan Hellkite");
        harness.assertNotOnBattlefield(player2, "Thundering Giant");
    }

    @Test
    @DisplayName("Deals 4 damage to each creature, killing small creatures and sparing large ones")
    void dealsFourDamageToEachCreature() {
        addLands(4);
        harness.addToBattlefield(player1, new ThunderingGiant());
        harness.addToBattlefield(player2, new ThunderingGiant());
        harness.addToBattlefield(player2, new ShivanHellkite());

        castWildfire();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Thundering Giant");
        harness.assertNotOnBattlefield(player2, "Thundering Giant");
        harness.assertOnBattlefield(player2, "Shivan Hellkite");
    }
}
