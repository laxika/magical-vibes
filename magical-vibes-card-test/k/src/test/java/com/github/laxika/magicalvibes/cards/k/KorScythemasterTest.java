package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KorScythemaster.class)
class KorScythemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike while attacking")
    void hasFirstStrikeWhileAttacking() {
        Permanent scythemaster = addCreatureReady(player1, new KorScythemaster());
        scythemaster.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, scythemaster, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike while not attacking")
    void doesNotHaveFirstStrikeWhileNotAttacking() {
        Permanent scythemaster = addCreatureReady(player1, new KorScythemaster());

        assertThat(gqs.hasKeyword(gd, scythemaster, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses first strike when it stops attacking")
    void losesFirstStrikeWhenItStopsAttacking() {
        Permanent scythemaster = addCreatureReady(player1, new KorScythemaster());
        scythemaster.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, scythemaster, Keyword.FIRST_STRIKE)).isTrue();

        scythemaster.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, scythemaster, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Attacking Scythemaster kills a blocking Scythemaster before it can deal damage")
    void attackingScythemasterSurvivesBlockingScythemaster() {
        Permanent attacker = addCreatureReady(player1, new KorScythemaster());
        Permanent blocker = addCreatureReady(player2, new KorScythemaster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Attacking does not grant first strike to another Scythemaster")
    void doesNotGrantFirstStrikeToOtherCreatures() {
        Permanent attacker = addCreatureReady(player1, new KorScythemaster());
        Permanent other = addCreatureReady(player1, new KorScythemaster());
        attacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }
}
