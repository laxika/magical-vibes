package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefenderOfTheQueue.class, GrizzlyBears.class, Plains.class})
class DefenderOfTheQueueTest extends BaseCardTest {

    @Test
    void boostsCreaturesImmediatelyToItsLeftAndRight() {
        Permanent left = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new DefenderOfTheQueue());
        Permanent right = addCreatureReady(player1, new GrizzlyBears());
        Permanent beyondRight = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, left)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, left)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, left, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, right)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, right)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, right, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, beyondRight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, beyondRight, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void noncreaturePermanentsDoNotInterruptCreatureAdjacency() {
        Permanent left = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        addCreatureReady(player1, new DefenderOfTheQueue());
        harness.addToBattlefield(player1, new Plains());
        Permanent right = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, left)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, left)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, left, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, right)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, right)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, right, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotBoostItselfOrOpponentsCreatures() {
        Permanent defender = addCreatureReady(player1, new DefenderOfTheQueue());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, defender)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, defender)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, defender, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void creatureBetweenTwoDefendersReceivesBothBonuses() {
        addCreatureReady(player1, new DefenderOfTheQueue());
        Permanent middle = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new DefenderOfTheQueue());

        assertThat(gqs.getEffectivePower(gd, middle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, middle)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, middle, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void enteringCreatureRequiresPositionChoiceWhileDefenderIsControlled() {
        Permanent left = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new DefenderOfTheQueue());
        Permanent right = addCreatureReady(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Between Grizzly Bears (1) and Defender of the Queue (2)");

        assertThat(gqs.getEffectivePower(gd, left)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, left, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, right)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, right, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }
}
