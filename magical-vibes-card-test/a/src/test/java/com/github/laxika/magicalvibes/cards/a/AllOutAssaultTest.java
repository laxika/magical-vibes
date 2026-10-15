package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllOutAssault.class, GrizzlyBears.class, Opalescence.class})
class AllOutAssaultTest extends BaseCardTest {

    @Test
    void boostsControlledCreaturesAndGrantsDeathtouch() {
        harness.addToBattlefield(player1, new AllOutAssault());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void enteringDuringMainPhaseAddsCombatAndMainPhaseAndSchedulesNextAttackUntap() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();

        castFromPostcombatMainPhase();

        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);

        declareAttackers(List.of(0));
        assertThat(attacker.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    void nextAttackUntapTriggerFiresOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castFromPostcombatMainPhase();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("All-Out Assault"));
    }

    @Test
    void animatedAssaultAlsoReceivesItsOwnBonus() {
        Permanent assault = harness.addToBattlefieldAndReturn(player1, new AllOutAssault());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, assault)).isTrue();
        assertThat(gqs.getEffectivePower(gd, assault)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, assault)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, assault, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void enteringOutsideMainPhaseDoesNotTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.enterBattlefieldAndReturn(player1, new AllOutAssault());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    @Test
    void enteringDuringOpponentsMainPhaseDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new AllOutAssault());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    @Test
    void declaringNoAttackersDoesNotConsumeDelayedUntap() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castFromPostcombatMainPhase();

        declareAttackers(List.of());
        assertThat(gd.stack).isEmpty();
        declareAttackers(List.of(0));
        assertThat(attacker.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void delayedUntapIncludesCreaturesEnteringLaterButNotOpposingCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        castFromPostcombatMainPhase();
        declareAttackers(List.of(0));
        Permanent laterCreature = addCreatureReady(player1, new GrizzlyBears());
        laterCreature.tap();
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        opposingCreature.tap();
        harness.passBothPriorities();

        assertThat(laterCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    void enteringDuringPrecombatMainPhaseAddsAnExtraCombatBeforeNormalCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new AllOutAssault(), "{2}{R}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
        declareAttackers(List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
    }

    @Test
    void unusedDelayedUntapExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castFromPostcombatMainPhase();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        declareAttackers(player1, List.of(0));

        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("All-Out Assault"));
    }
    private void castFromPostcombatMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new AllOutAssault(), "{2}{R}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
