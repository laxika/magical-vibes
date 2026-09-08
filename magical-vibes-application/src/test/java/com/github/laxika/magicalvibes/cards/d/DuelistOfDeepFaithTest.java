package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuelistOfDeepFaithTest extends BaseCardTest {

    @Test
    @DisplayName("Duelist of Deep Faith has first strike during its controller's turn")
    void hasFirstStrikeDuringItsControllersTurn() {
        Permanent duelist = addCreatureReady(player1, new DuelistOfDeepFaith());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Duelist of Deep Faith loses conditional first strike during the opponent's turn")
    void losesFirstStrikeDuringOpponentsTurn() {
        Permanent duelist = addCreatureReady(player1, new DuelistOfDeepFaith());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Duelist of Deep Faith gives a poison counter when it deals combat damage")
    void dealsToxicCombatDamage() {
        Permanent duelist = addCreatureReady(player1, new DuelistOfDeepFaith());
        duelist.setAttacking(true);

        resolveCombat(player1);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
