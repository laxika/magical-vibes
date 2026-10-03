package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuelistOfDeepFaith.class})
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

    @Test
    @DisplayName("Toxic gives poison as part of first-strike damage without using the stack")
    void toxicIsImmediateDamageResult() {
        Permanent duelist = addCreatureReady(player1, new DuelistOfDeepFaith());
        duelist.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prevented combat damage gives no poison counters")
    void preventedCombatDamageGivesNoPoison() {
        Permanent duelist = addCreatureReady(player1, new DuelistOfDeepFaith());
        duelist.setAttacking(true);
        harness.setLife(player2, 20);
        gd.preventAllCombatDamage = true;

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attacking Duelist kills a defending Duelist before it can deal damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new DuelistOfDeepFaith());
        Permanent blocker = addCreatureReady(player2, new DuelistOfDeepFaith());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Duelist of Deep Faith");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Duelist of Deep Faith");
        harness.assertNotOnBattlefield(player2, "Duelist of Deep Faith");
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
