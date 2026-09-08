package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeldonFirebombersTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesExcessLandsToSacrifice() {
        addLands(player1, 5);
        addLands(player2, 4);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        castKeldonFirebombers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 1));

        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void playersWithThreeOrFewerLandsDoNotSacrificeAny() {
        addLands(player1, 3);
        addLands(player2, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castKeldonFirebombers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castKeldonFirebombers() {
        harness.setHand(player1, List.of(new KeldonFirebombers()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, i % 2 == 0 ? new Forest() : new Mountain());
        }
    }

    private List<java.util.UUID> landIds(Player player, int count) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest
                        || permanent.getCard() instanceof Mountain)
                .limit(count)
                .map(Permanent::getId)
                .toList();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest
                        || permanent.getCard() instanceof Mountain)
                .count();
    }
}
