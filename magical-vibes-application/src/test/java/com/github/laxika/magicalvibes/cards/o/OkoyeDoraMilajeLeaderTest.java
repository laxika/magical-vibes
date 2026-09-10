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
}
