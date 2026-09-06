package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempestDrake.class, LongbowArcher.class, Warthog.class})
class TempestDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Tempest Drake untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent drake = addCreatureReady(player1, new TempestDrake());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Tempest Drake")
    void groundCannotBlockFlyer() {
        Permanent drake = addCreatureReady(player1, new TempestDrake());
        Permanent warthog = addCreatureReady(player2, new Warthog());
        drake.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, warthog), indexOf(player1, drake)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with flying can block Tempest Drake")
    void flyerCanBlockFlyer() {
        Permanent drake = addCreatureReady(player1, new TempestDrake());
        Permanent blocker = addCreatureReady(player2, new TempestDrake());
        drake.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, drake))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Tempest Drake")
    void creatureWithReachCanBlockFlyer() {
        Permanent drake = addCreatureReady(player1, new TempestDrake());
        Permanent archer = addCreatureReady(player2, new LongbowArcher());
        drake.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, archer), indexOf(player1, drake))));

        assertThat(archer.isBlocking()).isTrue();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
