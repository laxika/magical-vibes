package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoltariLancer.class, TrainedArmodon.class})
class SoltariLancerTest extends BaseCardTest {

    private Permanent addLancer() {
        return addCreatureReady(player1, new SoltariLancer());
    }

    @Test
    @DisplayName("Does not have first strike while not attacking")
    void noFirstStrikeWhileNotAttacking() {
        Permanent lancer = addLancer();

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has first strike while attacking")
    void hasFirstStrikeWhileAttacking() {
        Permanent lancer = addLancer();

        lancer.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike lets an attacking Lancer defeat a blocking Lancer")
    void firstStrikeDealsDamageBeforeBlockingLancer() {
        Permanent attacker = addLancer();
        Permanent blocker = addCreatureReady(player2, new SoltariLancer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Loses first strike when it stops attacking")
    void losesFirstStrikeWhenNotAttacking() {
        Permanent lancer = addLancer();
        lancer.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();

        lancer.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A creature without shadow cannot block Soltari Lancer")
    void cannotBeBlockedByCreatureWithoutShadow() {
        addLancer();
        addCreatureReady(player2, new TrainedArmodon());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Soltari Lancer cannot block a creature without shadow")
    void cannotBlockCreatureWithoutShadow() {
        addCreatureReady(player1, new TrainedArmodon());
        addCreatureReady(player2, new SoltariLancer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }
}
