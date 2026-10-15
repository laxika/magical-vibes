package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BelligerentSliver;
import com.github.laxika.magicalvibes.cards.g.GoblinBrawler;
import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.cards.s.SilentArbiter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorgrassScreen.class, GoblinBrawler.class, BelligerentSliver.class, SkyhunterProwler.class,
        SilentArbiter.class})
class RazorgrassScreenTest extends BaseCardTest {

    @Test
    @DisplayName("It must block each combat if able")
    void mustBlockEachCombat() {
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player1, new GoblinBrawler());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Blocking satisfies the requirement")
    void blockingSatisfiesRequirement() {
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player1, new GoblinBrawler());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A lone Screen is not able to block a menace attacker")
    void menaceMakesBlockingRequirementImpossible() {
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player1, new BelligerentSliver());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Enough blockers still have to block a menace attacker")
    void enoughBlockersMustBlockMenaceAttacker() {
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player1, new BelligerentSliver());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped Razorgrass Screen is not required to block")
    void noRequirementWhenTapped() {
        Permanent screen = addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player1, new GoblinBrawler());

        screen.tap();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A Screen is not required to block an attacker it cannot legally block")
    void noRequirementWhenAttackerHasFlying() {
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player1, new SkyhunterProwler());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Defender prevents Razorgrass Screen from attacking")
    void cannotAttackWithDefender() {
        addCreatureReady(player1, new RazorgrassScreen());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning sickness does not excuse the blocking requirement")
    void summoningSickScreenMustBlock() {
        harness.addToBattlefield(player2, new RazorgrassScreen());
        addCreatureReady(player1, new GoblinBrawler());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Blocking one of multiple attackers satisfies the requirement")
    void mayChooseWhichAttackerToBlock() {
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player1, new GoblinBrawler());
        addCreatureReady(player1, new GoblinBrawler());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Silent Arbiter allows one of two Screens to satisfy the maximum possible requirements")
    void oneScreenMayBlockWhenArbiterLimitsBlocking() {
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player1, new GoblinBrawler());
        harness.addToBattlefield(player1, new SilentArbiter());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
