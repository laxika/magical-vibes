package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftwingAssailant.class})
class SwiftwingAssailantTest extends BaseCardTest {

    @Test
    void gainsToughnessAndVigilanceAtMaxSpeed() {
        Permanent assailant = addCreatureReady(player1, new SwiftwingAssailant());
        int toughnessBeforeMaxSpeed = gqs.getEffectiveToughness(gd, assailant);

        assertThat(gqs.hasKeyword(gd, assailant, Keyword.VIGILANCE)).isFalse();

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(toughnessBeforeMaxSpeed + 1);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void castingStartsEnginesWithoutResettingExistingSpeed() {
        harness.castFromHand(player1, new SwiftwingAssailant(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();

        gd.playerSpeeds.put(player1.getId(), 3);
        harness.castFromHand(player1, new SwiftwingAssailant(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void bonusAppliesOnlyAtMaxSpeedAndDisappearsWhenSpeedDrops() {
        Permanent assailant = addCreatureReady(player1, new SwiftwingAssailant());
        int originalPower = gqs.getEffectivePower(gd, assailant);
        int originalToughness = gqs.getEffectiveToughness(gd, assailant);

        for (int speed : List.of(1, 2, 3, 4, 3)) {
            gd.playerSpeeds.put(player1.getId(), speed);

            assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(originalPower);
            assertThat(gqs.getEffectiveToughness(gd, assailant))
                    .isEqualTo(originalToughness + (speed == 4 ? 1 : 0));
            assertThat(gqs.hasKeyword(gd, assailant, Keyword.VIGILANCE)).isEqualTo(speed == 4);
        }
    }

    @Test
    void eachAssailantUsesItsOwnControllersSpeed() {
        Permanent own = addCreatureReady(player1, new SwiftwingAssailant());
        Permanent opposing = addCreatureReady(player2, new SwiftwingAssailant());
        int originalToughness = gqs.getEffectiveToughness(gd, own);
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);

        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, own, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(originalToughness + 1);
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void combatDamageReachesMaxSpeedOnlyAfterSpeedTriggerResolves() {
        Permanent assailant = addCreatureReady(player1, new SwiftwingAssailant());
        gd.playerSpeeds.put(player1.getId(), 3);
        int originalToughness = gqs.getEffectiveToughness(gd, assailant);
        int opponentsLife = gd.playerLifeTotals.get(player2.getId());
        int damage = gqs.getEffectivePower(gd, assailant);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.resolveCombatDamage();

        harness.assertLife(player2, opponentsLife - damage);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.VIGILANCE)).isFalse();

        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(originalToughness + 1);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.VIGILANCE)).isTrue();
        assertThat(assailant.isTapped()).isTrue();
    }

    @Test
    void attackingAtMaxSpeedDoesNotTap() {
        Permanent assailant = addCreatureReady(player1, new SwiftwingAssailant());
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(assailant.isAttacking()).isTrue();
        assertThat(assailant.isTapped()).isFalse();
    }

    @Test
    void attackingBelowMaxSpeedTaps() {
        Permanent assailant = addCreatureReady(player1, new SwiftwingAssailant());
        gd.playerSpeeds.put(player1.getId(), 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(assailant.isAttacking()).isTrue();
        assertThat(assailant.isTapped()).isTrue();
    }
}
