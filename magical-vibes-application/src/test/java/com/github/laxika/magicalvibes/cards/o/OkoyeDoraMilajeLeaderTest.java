package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OkoyeDoraMilajeLeader.class)
class OkoyeDoraMilajeLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("When Okoye enters, two Soldier tokens are created")
    void createsTwoSoldierTokensWhenEntering() {
        harness.enterBattlefieldAndReturn(player1, new OkoyeDoraMilajeLeader());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    @DisplayName("Attacking creature tokens you control have first strike")
    void grantsFirstStrikeToAttackingCreatureTokensYouControl() {
        Permanent okoye = harness.enterBattlefieldAndReturn(player1, new OkoyeDoraMilajeLeader());
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Soldier");
        Permanent attackingToken = tokens.getFirst();
        Permanent nonAttackingToken = tokens.getLast();
        attackingToken.setAttacking(true);
        okoye.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, attackingToken, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttackingToken, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, okoye, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An attacking token copy of Okoye grants itself first strike")
    void attackingTokenCopyGrantsItselfFirstStrike() {
        OkoyeDoraMilajeLeader tokenCopy = new OkoyeDoraMilajeLeader();
        tokenCopy.setToken(true);
        Permanent okoye = harness.addToBattlefieldAndReturn(player1, tokenCopy);
        okoye.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, okoye, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent attacking tokens do not gain first strike")
    void doesNotGrantFirstStrikeToOpponentTokens() {
        harness.addToBattlefield(player1, new OkoyeDoraMilajeLeader());
        harness.enterBattlefieldAndReturn(player2, new OkoyeDoraMilajeLeader());
        resolveAllTriggers();
        Permanent token = findPermanents(player2, "Soldier").getFirst();
        token.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player2.getId())
                .remove(findPermanent(player2, "Okoye, Dora Milaje Leader"));

        assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike ends when the token stops attacking or Okoye leaves")
    void firstStrikeTracksAttackingStatusAndSourcePresence() {
        Permanent okoye = harness.enterBattlefieldAndReturn(player1, new OkoyeDoraMilajeLeader());
        resolveAllTriggers();
        Permanent token = findPermanents(player1, "Soldier").getFirst();
        token.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isTrue();

        token.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isFalse();

        token.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(okoye);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isFalse();
    }
}
