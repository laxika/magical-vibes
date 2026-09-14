package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
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

@CardUsed({LumberingSatyr.class, FreshVolunteers.class, Forest.class})
class LumberingSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures, including Lumbering Satyr, have forestwalk")
    void grantsForestwalkToAllCreaturesIncludingItself() {
        Permanent satyr = harness.addToBattlefieldAndReturn(player1, new LumberingSatyr());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        assertThat(gqs.hasKeyword(gd, satyr, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("A creature entering later also has forestwalk")
    void grantsForestwalkToCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new LumberingSatyr());
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new FreshVolunteers());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Creatures lose forestwalk when Lumbering Satyr leaves")
    void forestwalkIsLostWhenLumberingSatyrLeaves() {
        Permanent satyr = harness.addToBattlefieldAndReturn(player1, new LumberingSatyr());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FORESTWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getId().equals(satyr.getId()));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Forestwalk prevents blocking while the defending player controls a Forest")
    void forestwalkPreventsBlockingWhenDefenderControlsForest() {
        Permanent attacker = addCreatureReady(player1, new LumberingSatyr());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player2, new Forest());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Forestwalk allows blocking when the defending player controls no Forest")
    void forestwalkAllowsBlockingWithoutDefendingForest() {
        Permanent attacker = addCreatureReady(player1, new LumberingSatyr());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestwalk checks only the defending player's lands")
    void forestwalkAllowsBlockingWhenOnlyAttackingPlayerControlsForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addCreatureReady(player1, new LumberingSatyr());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
