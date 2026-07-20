package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HonoredCropCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives +1/+0 to other attacking creatures")
    void boostsOtherAttackers() {
        Permanent captain = new Permanent(new HonoredCropCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(3); // 2 base + 1
        assertThat(bears.getEffectiveToughness()).isEqualTo(2); // unchanged
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostSelf() {
        Permanent captain = new Permanent(new HonoredCropCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

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
        Permanent captain = new Permanent(new HonoredCropCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Only the captain attacks; bears stays back.
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost opponent's attacking creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent captain = new Permanent(new HonoredCropCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(oppBears.getPowerModifier()).isEqualTo(0);
        assertThat(oppBears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtCleanup() {
        Permanent captain = new Permanent(new HonoredCropCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
    }
}
