package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlvenwaldOddity.class, UlvenwaldBehemoth.class, GrizzlyBears.class})
class UlvenwaldOddityTest extends BaseCardTest {

    @Test
    void transformsIntoUlvenwaldBehemoth() {
        Permanent oddity = addCreatureReady(player1, new UlvenwaldOddity());
        addTransformMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(oddity.isTransformed()).isTrue();
        assertThat(oddity.getCard()).isInstanceOf(UlvenwaldBehemoth.class);
    }

    @Test
    void backFaceBoostsOtherOwnCreaturesAndExcludesOpponentsAndItself() {
        Permanent oddity = addCreatureReady(player1, new UlvenwaldOddity());
        addTransformMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.HASTE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.HASTE)).isFalse();

        assertThat(gqs.getEffectivePower(gd, oddity)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, oddity)).isEqualTo(8);
    }

    private void addTransformMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
