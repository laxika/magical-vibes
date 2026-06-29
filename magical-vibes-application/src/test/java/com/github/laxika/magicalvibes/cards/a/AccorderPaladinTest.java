package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccorderPaladinTest extends BaseCardTest {

    // ===== Attack trigger =====

    @Test
    @DisplayName("Attacking with Accorder Paladin pushes battle cry trigger onto stack")
    void attackTriggerPushesOntoStack() {
        Permanent paladin = new Permanent(new AccorderPaladin());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Accorder Paladin");
        assertThat(entry.getSourcePermanentId()).isEqualTo(paladin.getId());
    }

    // ===== Battle cry boosts other attacking creatures =====

    @Test
    @DisplayName("Battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent paladin = new Permanent(new AccorderPaladin());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Both creatures attack
        gs.declareAttackers(gd, player1, List.of(0, 1));
        // Resolve battle cry trigger
        harness.passBothPriorities();

        // Grizzly Bears should get +1/+0 from battle cry
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(3); // 2 base + 1 battle cry
        assertThat(bears.getEffectiveToughness()).isEqualTo(2); // unchanged
    }

    @Test
    @DisplayName("Battle cry does not boost Accorder Paladin itself")
    void battleCryDoesNotBoostSelf() {
        Permanent paladin = new Permanent(new AccorderPaladin());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        // Accorder Paladin should NOT get its own battle cry boost
        assertThat(paladin.getPowerModifier()).isEqualTo(0);
        assertThat(paladin.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Battle cry does not boost non-attacking creatures")
    void battleCryDoesNotBoostNonAttackers() {
        Permanent paladin = new Permanent(new AccorderPaladin());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Only paladin attacks, bears stays back
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Bears should NOT get battle cry boost (not attacking)
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Battle cry does not boost opponent's creatures")
    void battleCryDoesNotBoostOpponentCreatures() {
        Permanent paladin = new Permanent(new AccorderPaladin());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);

        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Opponent's creature should NOT be affected
        assertThat(oppBears.getPowerModifier()).isEqualTo(0);
        assertThat(oppBears.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Multiple battle cry sources =====

    @Test
    @DisplayName("Two Accorder Paladins each give +1/+0 to the other and other attackers")
    void multipleBattleCrySourcesStack() {
        Permanent paladin1 = new Permanent(new AccorderPaladin());
        paladin1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin1);

        Permanent paladin2 = new Permanent(new AccorderPaladin());
        paladin2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // All three attack
        gs.declareAttackers(gd, player1, List.of(0, 1, 2));
        // Resolve both battle cry triggers
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Grizzly Bears gets +1/+0 from each paladin = +2/+0
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(4); // 2 base + 2

        // Each paladin gets +1/+0 from the OTHER paladin only
        assertThat(paladin1.getPowerModifier()).isEqualTo(1);
        assertThat(paladin2.getPowerModifier()).isEqualTo(1);
    }

    // ===== Boost resets at end of turn =====

    @Test
    @DisplayName("Battle cry boost resets at cleanup step")
    void boostResetsAtCleanup() {
        Permanent paladin = new Permanent(new AccorderPaladin());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        // Verify boost is applied
        assertThat(bears.getPowerModifier()).isEqualTo(1);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Boost should be gone
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
    }
}
