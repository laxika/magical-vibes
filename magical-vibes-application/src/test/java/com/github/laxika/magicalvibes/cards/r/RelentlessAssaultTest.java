package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelentlessAssault.class, Warthog.class})
class RelentlessAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts Relentless Assault on the stack as a sorcery with no target")
    void castingPutsItOnStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving untaps only creatures that attacked this turn")
    void resolvingUntapsOnlyAttackedCreatures() {
        Permanent attackedWarthog = addCreatureReady(player1, new Warthog());
        Permanent nonAttackedWarthog = addCreatureReady(player1, new Warthog());

        declareAttackers(List.of(0));
        nonAttackedWarthog.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(attackedWarthog.isTapped()).isFalse();
        assertThat(nonAttackedWarthog.isTapped()).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Additional combat begins after postcombat main when Relentless Assault resolves")
    void additionalCombatBeginsAfterPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Additional combat begins immediately after the precombat main phase")
    void additionalCombatBeginsAfterPrecombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    @Test
    @DisplayName("Attacked-this-turn status resets on turn change")
    void attackedThisTurnResetsOnTurnChange() {
        Permanent bear = addCreatureReady(player1, new Warthog());

        declareAttackers(List.of(0));
        assertThat(bear.isAttackedThisTurn()).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);
        assertThat(bear.isAttackedThisTurn()).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        bear.tap();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
    }
}

