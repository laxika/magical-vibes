package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BerserkersOnslaught.class, GrizzlyBears.class})
class BerserkersOnslaughtTest extends BaseCardTest {

    @Test
    void attackingCreatureYouControlGainsDoubleStrike() {
        harness.addToBattlefield(player1, new BerserkersOnslaught());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void nonattackingAndOpponentsCreaturesDoNotGainDoubleStrike() {
        harness.addToBattlefield(player1, new BerserkersOnslaught());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentAttacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void creatureLosesGrantedDoubleStrikeWhenItStopsAttacking() {
        harness.addToBattlefield(player1, new BerserkersOnslaught());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        creature.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
