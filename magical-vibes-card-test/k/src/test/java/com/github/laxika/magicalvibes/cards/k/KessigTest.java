package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Kessig.class, GrizzlyBears.class})
class KessigTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Kessig(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void preventsCombatDamageByNonWerewolfCreatures() {
        harness.setLife(player1, 20);
        addAttacker(player2);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void chaosBoostsYourCreaturesAndMakesThemWerewolvesUntilEndOfTurn() {
        Permanent attacker = addAttacker(player1);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gqs.effectiveCreatureSubtypes(gd, attacker)).contains(CardSubtype.WEREWOLF);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void chaosAffectsOnlyCreaturesControlledAtResolution() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        for (Permanent affected : new Permanent[]{own, beforeResolution}) {
            assertThat(gqs.getEffectivePower(gd, affected)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, affected)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, affected, Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.effectiveCreatureSubtypes(gd, affected))
                    .contains(CardSubtype.BEAR, CardSubtype.WEREWOLF);
        }
        for (Permanent unaffected : new Permanent[]{opponent, afterResolution}) {
            assertThat(gqs.getEffectivePower(gd, unaffected)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, unaffected)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.TRAMPLE)).isFalse();
            assertThat(gqs.effectiveCreatureSubtypes(gd, unaffected)).doesNotContain(CardSubtype.WEREWOLF);
        }
    }

    @Test
    void chaosBonusesStackAndAllExpireAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.WEREWOLF);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .contains(CardSubtype.BEAR).doesNotContain(CardSubtype.WEREWOLF);
    }

    @Test
    void preventionEndsWhenKessigLeavesTheCommandZone() {
        addAttacker(player1);
        harness.setLife(player2, 20);
        harness.inMutationScope(() -> gd.planechase.faceUp.clear());

        resolveCombat(player1);

        harness.assertLife(player2, 18);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.getId().equals(player1.getId()) ? player2.getId() : player1.getId());
        return attacker;
    }
}
