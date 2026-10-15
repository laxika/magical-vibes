package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
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

@CardUsed({NoggleBandit.class, AngelicWall.class, GrizzlyBears.class})
class NoggleBanditTest extends BaseCardTest {

    @Test
    @DisplayName("Noggle Bandit cannot be blocked by a creature without defender")
    void cannotBeBlockedByNonDefender() {
        Permanent bandit = addCreatureReady(player1, new NoggleBandit());
        bandit.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with defender");
    }

    @Test
    @DisplayName("Noggle Bandit can be blocked by a creature with defender")
    void canBeBlockedByDefender() {
        Permanent bandit = addCreatureReady(player1, new NoggleBandit());
        bandit.setAttacking(true);

        Permanent wall = addCreatureReady(player2, new AngelicWall());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Multiple creatures with defender can block Noggle Bandit")
    void canBeBlockedByMultipleDefenders() {
        Permanent bandit = addCreatureReady(player1, new NoggleBandit());
        bandit.setAttacking(true);
        Permanent firstWall = addCreatureReady(player2, new AngelicWall());
        Permanent secondWall = addCreatureReady(player2, new AngelicWall());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstWall.isBlocking()).isTrue();
        assertThat(secondWall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Noggle Bandit's restriction does not affect another attacker")
    void nonDefenderCanBlockAnotherAttacker() {
        Permanent bandit = addCreatureReady(player1, new NoggleBandit());
        bandit.setAttacking(true);
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());
        attackingBears.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new AngelicWall());
        Permanent blockingBears = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(wall.isBlocking()).isTrue();
        assertThat(blockingBears.isBlocking()).isTrue();
    }
}
