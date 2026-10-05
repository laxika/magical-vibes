package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArmoredPegasus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KeenEyedArchers.class, ArmoredPegasus.class, GrizzlyBears.class})
class KeenEyedArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Keen-Eyed Archers block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new ArmoredPegasus());
        Permanent archers = addCreatureReady(player2, new KeenEyedArchers());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, flyer)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, archers), indexOf(player1, flyer))));

        assertThat(archers.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block the flyer")
    void nonReachCannotBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new ArmoredPegasus());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, flyer)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, bears), indexOf(player1, flyer)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reach also allows blocking a creature without flying")
    void reachCanBlockGroundCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent archers = addCreatureReady(player2, new KeenEyedArchers());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, bears)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, archers), indexOf(player1, bears))));

        assertThat(archers.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach does not prevent creatures without flying from blocking the Archers")
    void reachDoesNotGrantFlyingWhenAttacking() {
        Permanent archers = addCreatureReady(player1, new KeenEyedArchers());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, archers)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, bears), indexOf(player1, archers))));

        assertThat(bears.isBlocking()).isTrue();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
