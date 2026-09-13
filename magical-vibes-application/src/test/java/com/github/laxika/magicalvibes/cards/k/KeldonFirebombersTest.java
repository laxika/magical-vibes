package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeldonFirebombers.class, KeldonBerserker.class, RhysticCave.class,
        WintermoonMesa.class})
class KeldonFirebombersTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesExcessLandsToSacrifice() {
        addLands(player1, 5);
        addLands(player2, 4);
        harness.addToBattlefield(player1, new KeldonBerserker());
        harness.addToBattlefield(player2, new KeldonBerserker());
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
        assertThat(landCount(player1)).isEqualTo(5);

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 1));

        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Keldon Berserker");
        harness.assertOnBattlefield(player2, "Keldon Berserker");
    }

    @Test
    void playersWithThreeOrFewerLandsDoNotSacrificeAny() {
        addLands(player1, 3);
        addLands(player2, 2);
        harness.addToBattlefield(player2, new KeldonBerserker());

        castKeldonFirebombers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Keldon Berserker");
    }

    @CardUsed({TajuruPreserver.class})
    @Test
    void opponentCannotCauseProtectedPlayerToSacrificeLands() {
        addLands(player1, 3);
        addLands(player2, 4);
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.forceActivePlayer(player1);

        castKeldonFirebombers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Tajuru Preserver");
    }

    @CardUsed(TajuruPreserver.class)
    @Test
    void sacrificeProtectionDoesNotPreventYourOwnAbility() {
        addLands(player1, 4);
        addLands(player2, 3);
        harness.addToBattlefield(player1, new TajuruPreserver());
        harness.forceActivePlayer(player1);

        castKeldonFirebombers();
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(3);
    }

    private void castKeldonFirebombers() {
        harness.castFromHand(player1, new KeldonFirebombers(), "{3}{R}{R}");
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, i % 2 == 0 ? new RhysticCave() : new WintermoonMesa());
        }
    }

    private List<java.util.UUID> landIds(Player player, int count) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RhysticCave
                        || permanent.getCard() instanceof WintermoonMesa)
                .limit(count)
                .map(Permanent::getId)
                .toList();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RhysticCave
                        || permanent.getCard() instanceof WintermoonMesa)
                .count();
    }
}
