package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HonoredCropCaptain.class, PouncingCheetah.class})
class HonoredCropCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives +1/+0 to other attacking creatures")
    void boostsOtherAttackers() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HonoredCropCaptain());
        captain.setSummoningSick(false);

        Permanent cheetah = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        cheetah.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(0);
        assertThat(cheetah.getEffectivePower()).isEqualTo(4); // 3 base + 1
        assertThat(cheetah.getEffectiveToughness()).isEqualTo(2); // unchanged
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostSelf() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HonoredCropCaptain());
        captain.setSummoningSick(false);

        Permanent cheetah = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        cheetah.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost non-attacking creatures")
    void doesNotBoostNonAttackers() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HonoredCropCaptain());
        captain.setSummoningSick(false);

        Permanent cheetah = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        cheetah.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Only the captain attacks; cheetah stays back.
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(cheetah.getPowerModifier()).isEqualTo(0);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HonoredCropCaptain());
        captain.setSummoningSick(false);

        Permanent oppCheetah = harness.addToBattlefieldAndReturn(player2, new PouncingCheetah());
        oppCheetah.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(oppCheetah.getPowerModifier()).isEqualTo(0);
        assertThat(oppCheetah.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Two attacking captains boost each other independently")
    void twoCaptainsBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HonoredCropCaptain());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HonoredCropCaptain());
        first.setSummoningSick(false);
        second.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(first.getPowerModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attack trigger still boosts other attackers after the captain dies")
    void triggerResolvesAfterSourceDies() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HonoredCropCaptain());
        Permanent cheetah = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        captain.setSummoningSick(false);
        cheetah.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1));

        captain.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(captain);
        harness.passBothPriorities();

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtCleanup() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HonoredCropCaptain());
        captain.setSummoningSick(false);

        Permanent cheetah = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        cheetah.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cheetah.getPowerModifier()).isEqualTo(0);
        assertThat(cheetah.getEffectivePower()).isEqualTo(3);
    }
}
