package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreshFacedRecruit.class})
class FreshFacedRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllersTurn() {
        Permanent recruit = addCreatureReady(player1, new FreshFacedRecruit());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during its controller's opponent's turn")
    void noFirstStrikeDuringOpponentsTurn() {
        Permanent recruit = addCreatureReady(player1, new FreshFacedRecruit());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike updates immediately when the active player changes")
    void firstStrikeUpdatesAcrossTurns() {
        Permanent recruit = addCreatureReady(player1, new FreshFacedRecruit());
        Permanent opposingRecruit = addCreatureReady(player2, new FreshFacedRecruit());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingRecruit, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingRecruit, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingRecruit, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An attacking Recruit kills a blocking Recruit before receiving damage")
    void attackingRecruitDealsFirstStrikeDamage() {
        Permanent attacker = addCreatureReady(player1, new FreshFacedRecruit());
        Permanent blocker = addCreatureReady(player2, new FreshFacedRecruit());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A defending Recruit has no first strike while the opponent attacks")
    void defendingRecruitDoesNotDealFirstStrikeDamage() {
        Permanent attacker = addCreatureReady(player2, new FreshFacedRecruit());
        Permanent blocker = addCreatureReady(player1, new FreshFacedRecruit());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An unblocked Recruit deals damage only once")
    void unblockedRecruitDealsDamageOnlyOnce() {
        Permanent recruit = addCreatureReady(player1, new FreshFacedRecruit());
        recruit.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
