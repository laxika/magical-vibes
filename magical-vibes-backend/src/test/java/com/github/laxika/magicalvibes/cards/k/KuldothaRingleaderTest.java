package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KuldothaRingleaderTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Kuldotha Ringleader has MustAttackEffect")
    void hasMustAttackEffect() {
        KuldothaRingleader card = new KuldothaRingleader();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MustAttackEffect.class);
    }

    // ===== Must attack =====

    @Test
    @DisplayName("Kuldotha Ringleader must attack when able")
    void mustAttackWhenAble() {
        Permanent ringleader = new Permanent(new KuldothaRingleader());
        ringleader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ringleader);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Kuldotha Ringleader can be declared as attacker and deals 4 damage")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        Permanent ringleader = new Permanent(new KuldothaRingleader());
        ringleader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ringleader);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities(); // resolve battle cry trigger (no other attackers to boost)

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Kuldotha Ringleader does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        Permanent ringleader = new Permanent(new KuldothaRingleader());
        // summoning sick by default
        gd.playerBattlefields.get(player1.getId()).add(ringleader);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Only bears can attack (index 1), ringleader has summoning sickness
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Battle cry =====

    @Test
    @DisplayName("Attacking with Kuldotha Ringleader pushes battle cry trigger onto stack")
    void attackTriggerPushesOntoStack() {
        Permanent ringleader = new Permanent(new KuldothaRingleader());
        ringleader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ringleader);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Kuldotha Ringleader");
    }

    @Test
    @DisplayName("Battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent ringleader = new Permanent(new KuldothaRingleader());
        ringleader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ringleader);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities(); // resolve battle cry trigger

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(3); // 2 base + 1 battle cry
    }

    @Test
    @DisplayName("Battle cry does not boost Kuldotha Ringleader itself")
    void battleCryDoesNotBoostSelf() {
        Permanent ringleader = new Permanent(new KuldothaRingleader());
        ringleader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ringleader);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(ringleader.getPowerModifier()).isEqualTo(0);
        assertThat(ringleader.getToughnessModifier()).isEqualTo(0);
    }
}
