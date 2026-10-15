package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarbinVanguardAviator.class, GrizzlyBears.class, YotianSoldier.class, Humble.class})
class HarbinVanguardAviatorTest extends BaseCardTest {

    @Test
    void doesNotGrantFlyingToHarbinControlledByOpponentAtResolution() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new YotianSoldier());
        }

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2, 3, 4));
            assertThat(gd.stack).hasSize(1);
            harness.setHand(player1, List.of(new Humble()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castInstant(player1, 0, harbin.getId());
            harness.passBothPriorities();
            assertThat(gd.stack).hasSize(1);
            assertThat(gqs.hasKeyword(gd, harbin, Keyword.FLYING)).isFalse();

            gd.playerBattlefields.get(player1.getId()).remove(harbin);
            gd.playerBattlefields.get(player2.getId()).add(harbin);
            harbin.setAttacking(false);
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, harbin, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, harbin)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, harbin)).isEqualTo(1);
    }

    @Test
    void triggersOnceWithMoreThanFiveSoldiersWhileHarbinDoesNotAttack() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new YotianSoldier());
        }
        int harbinPower = gqs.getEffectivePower(gd, harbin);
        int soldierPower = gqs.getEffectivePower(gd, soldier);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1, 2, 3, 4, 5, 6));
            assertThat(gd.stack).hasSize(1);
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, harbin)).isEqualTo(harbinPower + 1);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower + 1);
        assertThat(soldier.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void stillResolvesAfterAnAttackingSoldierLeavesTheBattlefield() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        Permanent departingSoldier = addCreatureReady(player1, new YotianSoldier());
        Permanent remainingSoldier = addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new YotianSoldier());
        int harbinPower = gqs.getEffectivePower(gd, harbin);
        int soldierPower = gqs.getEffectivePower(gd, remainingSoldier);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2, 3, 4));
            assertThat(gd.stack).hasSize(1);
            gd.playerBattlefields.get(player1.getId()).remove(departingSoldier);
            gd.playerGraveyards.get(player1.getId()).add(departingSoldier.getCard());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, harbin)).isEqualTo(harbinPower + 1);
        assertThat(gqs.getEffectivePower(gd, remainingSoldier)).isEqualTo(soldierPower + 1);
        assertThat(remainingSoldier.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void stillResolvesAfterHarbinLeavesTheBattlefield() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new YotianSoldier());
        }
        int soldierPower = gqs.getEffectivePower(gd, soldier);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2, 3, 4));
            assertThat(gd.stack).hasSize(1);
            gd.playerBattlefields.get(player1.getId()).remove(harbin);
            gd.playerGraveyards.get(player1.getId()).add(harbin.getCard());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower + 1);
        assertThat(soldier.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void affectsCreaturesPresentAtResolutionAndExpiresAtEndOfTurn() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new YotianSoldier());
        }
        int harbinPower = gqs.getEffectivePower(gd, harbin);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2, 3, 4));
            assertThat(gd.stack).hasSize(1);
            Permanent earlyCreature = addCreatureReady(player1, new GrizzlyBears());
            int earlyPower = gqs.getEffectivePower(gd, earlyCreature);
            int earlyToughness = gqs.getEffectiveToughness(gd, earlyCreature);
            resolveAllTriggers();

            assertThat(gqs.getEffectivePower(gd, earlyCreature)).isEqualTo(earlyPower + 1);
            assertThat(gqs.getEffectiveToughness(gd, earlyCreature)).isEqualTo(earlyToughness + 1);
            assertThat(earlyCreature.hasKeyword(Keyword.FLYING)).isTrue();

            Permanent lateCreature = addCreatureReady(player1, new GrizzlyBears());
            assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(earlyPower);
            assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(earlyToughness);
            assertThat(lateCreature.hasKeyword(Keyword.FLYING)).isFalse();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passUntil(player2, TurnStep.UPKEEP);

            assertThat(gqs.getEffectivePower(gd, earlyCreature)).isEqualTo(earlyPower);
            assertThat(gqs.getEffectiveToughness(gd, earlyCreature)).isEqualTo(earlyToughness);
            assertThat(earlyCreature.hasKeyword(Keyword.FLYING)).isFalse();
            assertThat(gqs.getEffectivePower(gd, harbin)).isEqualTo(harbinPower);
            assertThat(harbin.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Buffs and gives flying to creatures you control after attacking with five Soldiers")
    void buffsCreaturesAfterAttackingWithFiveSoldiers() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        Permanent soldier1 = addCreatureReady(player1, new YotianSoldier());
        Permanent soldier2 = addCreatureReady(player1, new YotianSoldier());
        Permanent soldier3 = addCreatureReady(player1, new YotianSoldier());
        Permanent soldier4 = addCreatureReady(player1, new YotianSoldier());
        Permanent nonattackingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        int harbinPower = gqs.getEffectivePower(gd, harbin);
        int harbinToughness = gqs.getEffectiveToughness(gd, harbin);
        int nonattackingCreaturePower = gqs.getEffectivePower(gd, nonattackingCreature);

        declareAttackers(List.of(0, 1, 2, 3, 4));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, harbin)).isEqualTo(harbinPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, harbin)).isEqualTo(harbinToughness + 1);
        assertThat(harbin.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(soldier1.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(soldier2.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(soldier3.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(soldier4.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonattackingCreature)).isEqualTo(nonattackingCreaturePower + 1);
        assertThat(nonattackingCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when five creatures attack but fewer than five are Soldiers")
    void doesNotTriggerWithFewerThanFiveSoldiers() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new GrizzlyBears());

        int harbinPower = gqs.getEffectivePower(gd, harbin);
        int harbinToughness = gqs.getEffectiveToughness(gd, harbin);

        declareAttackers(List.of(0, 1, 2, 3, 4));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, harbin)).isEqualTo(harbinPower);
        assertThat(gqs.getEffectiveToughness(gd, harbin)).isEqualTo(harbinToughness);
        assertThat(soldier.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
