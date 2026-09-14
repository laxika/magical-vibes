package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mutilate.class, Swamp.class, WildMongrel.class})
class MutilateTest extends BaseCardTest {

    private void putMutilateOnStack() {
        harness.castFromHand(player1, new Mutilate(), "{2}{B}{B}");
    }

    private void castMutilate() {
        putMutilateOnStack();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gives all creatures -1/-1 for each Swamp the caster controls")
    void debuffsAllCreaturesPerSwamp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent ownMongrel = harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        Permanent enemyMongrel = harness.addToBattlefieldAndReturn(player2, new WildMongrel());

        castMutilate();

        assertThat(gqs.getEffectivePower(gd, ownMongrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownMongrel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enemyMongrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyMongrel)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills creatures whose toughness drops to zero")
    void killsCreaturesWithEnoughSwamps() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new WildMongrel());
        harness.addToBattlefield(player2, new WildMongrel());

        castMutilate();

        harness.assertNotOnBattlefield(player1, "Wild Mongrel");
        harness.assertNotOnBattlefield(player2, "Wild Mongrel");
    }

    @Test
    @DisplayName("Does nothing when the caster controls no Swamps")
    void noSwampsNoEffect() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent mongrel = harness.addToBattlefieldAndReturn(player2, new WildMongrel());

        castMutilate();

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts the caster's Swamps when it resolves")
    void countsSwampsAtResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent mongrel = harness.addToBattlefieldAndReturn(player2, new WildMongrel());

        putMutilateOnStack();
        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Swamp());
        Permanent mongrel = harness.addToBattlefieldAndReturn(player2, new WildMongrel());

        castMutilate();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(2);
    }
}
