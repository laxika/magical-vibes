package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TectonicHellion.class, Forest.class, Mountain.class})
class TectonicHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Only the player tied for most lands sacrifices two lands")
    void onlyLandLeaderSacrificesTwoLands() {
        addLands(player1, Mountain.class, 4);
        addLands(player2, Forest.class, 2);
        Permanent hellion = addCreatureReady(player1, new TectonicHellion());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hellion)));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, choice.validIds().subList(0, 2));

        assertThat(landCount(player1)).isEqualTo(2);
        assertThat(landCount(player2)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Players tied for most lands each choose two lands to sacrifice")
    void tiedLandLeadersEachSacrificeTwoLands() {
        addLands(player1, Mountain.class, 3);
        addLands(player2, Forest.class, 3);
        Permanent hellion = addCreatureReady(player1, new TectonicHellion());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hellion)));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, player1Choice.validIds().subList(0, 2));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2, player2Choice.validIds().subList(0, 2));

        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(landCount(player2)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addLands(Player player, Class<? extends com.github.laxika.magicalvibes.model.Card> landClass,
            int count) {
        for (int i = 0; i < count; i++) {
            if (landClass == Mountain.class) {
                harness.addToBattlefield(player, new Mountain());
            } else {
                harness.addToBattlefield(player, new Forest());
            }
        }
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> gqs.isLand(gd, permanent))
                .count();
    }
}
