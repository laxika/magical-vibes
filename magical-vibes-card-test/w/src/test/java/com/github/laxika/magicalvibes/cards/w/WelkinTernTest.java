package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SentinelSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WelkinTern.class, SerraAngel.class, WalkingCorpse.class, SentinelSpider.class})
class WelkinTernTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Welkin Tern onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new WelkinTern()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Welkin Tern");
    }

    @Test
    @DisplayName("Welkin Tern can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent ternPerm = addCreatureReady(player2, new WelkinTern());

        Permanent atkPerm = addCreatureReady(player1, new SerraAngel());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(ternPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Welkin Tern cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        Permanent ternPerm = addCreatureReady(player2, new WelkinTern());

        Permanent atkPerm = addCreatureReady(player1, new WalkingCorpse());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
        assertThat(ternPerm.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Welkin Tern has flying and can't be blocked by a ground creature")
    void cannotBeBlockedByGroundCreature() {
        Permanent atkPerm = addCreatureReady(player1, new WelkinTern());
        atkPerm.setAttacking(true);

        addCreatureReady(player2, new WalkingCorpse());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reachCreatureCanBlockWelkinTern() {
        addCreatureReady(player1, new WelkinTern());
        Permanent spider = addCreatureReady(player2, new SentinelSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void cannotBlockCreatureWithReachButWithoutFlying() {
        addCreatureReady(player1, new SentinelSpider());
        Permanent tern = addCreatureReady(player2, new WelkinTern());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
        assertThat(tern.isBlocking()).isFalse();
    }

    @Test
    void flyingCreatureCanBlockWelkinTern() {
        addCreatureReady(player1, new WelkinTern());
        Permanent angel = addCreatureReady(player2, new SerraAngel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(angel.isBlocking()).isTrue();
    }
}
