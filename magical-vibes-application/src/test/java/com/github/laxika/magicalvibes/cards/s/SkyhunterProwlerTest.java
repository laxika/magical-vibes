package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyhunterProwler.class, GiantSpider.class, GrizzlyBears.class, SerraAngel.class})
class SkyhunterProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SkyhunterProwler());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Flying lets a creature block Skyhunter Prowler")
    void flyingLetsCreatureBlockSkyhunterProwler() {
        addCreatureReady(player1, new SkyhunterProwler());
        Permanent blocker = addCreatureReady(player2, new SerraAngel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach lets a creature block Skyhunter Prowler")
    void reachLetsCreatureBlockSkyhunterProwler() {
        addCreatureReady(player1, new SkyhunterProwler());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Skyhunter Prowler untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent prowler = addCreatureReady(player1, new SkyhunterProwler());

        declareAttackers(List.of(0));

        assertThat(prowler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Skyhunter Prowler can block a creature without flying")
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent prowler = addCreatureReady(player2, new SkyhunterProwler());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(prowler.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Skyhunter Prowler to attack")
    void tappedProwlerCannotAttack() {
        Permanent prowler = addCreatureReady(player1, new SkyhunterProwler());
        prowler.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prowler.isAttacking()).isFalse();
        assertThat(prowler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick Skyhunter Prowler to attack")
    void summoningSickProwlerCannotAttack() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new SkyhunterProwler());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prowler.isAttacking()).isFalse();
    }
}
