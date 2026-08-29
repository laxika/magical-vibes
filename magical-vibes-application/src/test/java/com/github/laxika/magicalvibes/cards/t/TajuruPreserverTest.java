package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TajuruPreserverTest extends BaseCardTest {

    private long creatureCount(Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
    }

    @Test
    @DisplayName("An opponent's targeted edict can't make Tajuru Preserver's controller sacrifice")
    void opponentTargetedEdictDoesNothing() {
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Tajuru Preserver");
    }

    @Test
    @DisplayName("An opponent's each-player edict skips Tajuru Preserver's controller")
    void opponentEachPlayerEdictSkipsProtectedPlayer() {
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BarterInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isEqualTo(3);
    }
}
