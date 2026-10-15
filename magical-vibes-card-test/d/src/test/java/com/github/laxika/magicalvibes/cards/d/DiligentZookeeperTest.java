package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.b.BallistaSquad;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiligentZookeeper.class, AvenCloudchaser.class, BallistaSquad.class, AmoeboidChangeling.class})
class DiligentZookeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Gives non-Human creatures +1/+1 for each of their creature types")
    void boostsNonHumanCreaturesByTheirCreatureTypeCount() {
        harness.addToBattlefield(player1, new DiligentZookeeper());
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new AvenCloudchaser());
        Permanent ballista = harness.addToBattlefieldAndReturn(player1, new BallistaSquad());

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ballista)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ballista)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost a Changeling because it is Human")
    void doesNotBoostChangelingBecauseItIsHuman() {
        harness.addToBattlefield(player1, new DiligentZookeeper());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost Human creatures or creatures controlled by an opponent")
    void excludesHumansAndOpponents() {
        harness.addToBattlefield(player1, new DiligentZookeeper());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new BallistaSquad());
        Permanent opponentAven = harness.addToBattlefieldAndReturn(player2, new AvenCloudchaser());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentAven)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentAven)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Zookeepers independently boost eligible creatures")
    void multipleZookeepersStack() {
        harness.addToBattlefield(player1, new DiligentZookeeper());
        harness.addToBattlefield(player1, new DiligentZookeeper());
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new AvenCloudchaser());

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(6);
    }

    @Test
    @DisplayName("Gaining all creature types removes the bonus until cleanup because the creature becomes Human")
    void gainingHumanTypeRemovesBonusUntilCleanup() {
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new DiligentZookeeper());
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new AvenCloudchaser());
        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(4);

        harness.activateAbility(player1, 0, 0, null, aven.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature with no creature types gets no bonus until its types return")
    void losingAllCreatureTypesRemovesBonusUntilCleanup() {
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new DiligentZookeeper());
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new AvenCloudchaser());
        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(4);

        harness.activateAbility(player1, 0, 1, null, aven.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(4);
    }
}
