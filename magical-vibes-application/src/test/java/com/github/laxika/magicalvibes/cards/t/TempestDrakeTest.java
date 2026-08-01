package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TempestDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Tempest Drake untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent drake = addReady(player1, new TempestDrake());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Tempest Drake")
    void groundCannotBlockFlyer() {
        Permanent drake = addReady(player1, new TempestDrake());
        Permanent bears = addReady(player2, new GrizzlyBears());
        drake.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, bears), indexOf(player1, drake)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with flying can block Tempest Drake")
    void flyerCanBlockFlyer() {
        Permanent drake = addReady(player1, new TempestDrake());
        Permanent hawk = addReady(player2, new SuntailHawk());
        drake.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, hawk), indexOf(player1, drake))));

        assertThat(hawk.isBlocking()).isTrue();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
