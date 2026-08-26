package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThundersongTrumpeter.class, GrizzlyBears.class})
class ThundersongTrumpeterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it prevents the target creature from attacking or blocking this turn")
    void targetCannotAttackOrBlockThisTurn() {
        Permanent trumpeter = addCreatureReady(player1, new ThundersongTrumpeter());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(trumpeter.isTapped()).isTrue();
        assertThat(als.canAttack(gd, bears, player2.getId())).isFalse();
        assertThat(bls.canBlock(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("The ability does not affect other creatures")
    void otherCreaturesAreUnaffected() {
        addCreatureReady(player1, new ThundersongTrumpeter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, other, player2.getId())).isTrue();
        assertThat(bls.canBlock(gd, other)).isTrue();
    }
}
