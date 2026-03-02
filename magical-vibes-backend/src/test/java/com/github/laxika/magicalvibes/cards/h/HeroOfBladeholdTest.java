package com.github.laxika.magicalvibes.cards.h;

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

class HeroOfBladeholdTest extends BaseCardTest {

    // ===== Attack trigger: token creation =====

    @Test
    @DisplayName("Attacking with Hero of Bladehold creates two 1/1 Soldier tokens tapped and attacking")
    void attackCreatesTokensTappedAndAttacking() {
        Permanent hero = new Permanent(new HeroOfBladehold());
        hero.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hero);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Stack should have: token creation trigger + battle cry trigger
        assertThat(gd.stack).hasSize(2);

        // Resolve battle cry first (it's on top of stack)
        harness.passBothPriorities();

        // Now resolve token creation trigger
        harness.passBothPriorities();

        // Two Soldier tokens should be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long soldierTokenCount = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"))
                .count();
        assertThat(soldierTokenCount).isEqualTo(2);

        // Tokens should be tapped and have attacked this turn
        // (attacking flag is cleared at end of combat, but attackedThisTurn persists)
        battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"))
                .forEach(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttackedThisTurn()).isTrue();
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Hero of Bladehold attack trigger puts both triggers on stack")
    void attackPutsTriggersOnStack() {
        Permanent hero = new Permanent(new HeroOfBladehold());
        hero.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hero);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Both token creation trigger and battle cry trigger should be on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Hero of Bladehold");
        });
    }

    // ===== Battle cry interaction =====

    @Test
    @DisplayName("Battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent hero = new Permanent(new HeroOfBladehold());
        hero.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hero);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));

        // Resolve battle cry trigger (on top of stack)
        harness.passBothPriorities();

        // Bears should get +1/+0 from battle cry
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Battle cry does not boost Hero of Bladehold itself")
    void battleCryDoesNotBoostSelf() {
        Permanent hero = new Permanent(new HeroOfBladehold());
        hero.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hero);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve battle cry trigger
        harness.passBothPriorities();

        // Hero should NOT get its own battle cry boost
        assertThat(hero.getPowerModifier()).isEqualTo(0);
    }

    // ===== Token characteristics =====

    @Test
    @DisplayName("Soldier tokens are white creature tokens")
    void soldierTokensAreWhiteCreatures() {
        Permanent hero = new Permanent(new HeroOfBladehold());
        hero.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hero);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve both triggers
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"))
                .forEach(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.WHITE);
                    assertThat(token.getCard().getType()).isEqualTo(com.github.laxika.magicalvibes.model.CardType.CREATURE);
                    assertThat(token.getCard().getSubtypes()).contains(com.github.laxika.magicalvibes.model.CardSubtype.SOLDIER);
                });
    }
}
