package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyshroudFalcon.class, GrizzlyBears.class, GiantSpider.class})
class SkyshroudFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Skyshroud Falcon")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SkyshroudFalcon());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Vigilance keeps Skyshroud Falcon untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent falcon = addCreatureReady(player1, new SkyshroudFalcon());

        declareAttackers(List.of(0));

        assertThat(falcon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature with flying can block Skyshroud Falcon")
    void flyingCreatureCanBlockSkyshroudFalcon() {
        addCreatureReady(player1, new SkyshroudFalcon());
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Skyshroud Falcon")
    void reachCreatureCanBlockSkyshroudFalcon() {
        addCreatureReady(player1, new SkyshroudFalcon());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Skyshroud Falcon can block a creature without flying")
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Skyshroud Falcon to attack")
    void tappedFalconCannotAttack() {
        Permanent falcon = addCreatureReady(player1, new SkyshroudFalcon());
        falcon.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(falcon.isTapped()).isTrue();
        assertThat(falcon.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick Skyshroud Falcon to attack")
    void summoningSickFalconCannotAttack() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new SkyshroudFalcon());
        falcon.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(falcon.isAttacking()).isFalse();
    }
}
