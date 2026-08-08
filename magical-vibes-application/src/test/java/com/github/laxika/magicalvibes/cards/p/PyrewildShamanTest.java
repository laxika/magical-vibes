package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PyrewildShamanTest extends BaseCardTest {

    private Card putShamanInGraveyard() {
        Card shaman = new PyrewildShaman();
        gd.playerGraveyards.get(player1.getId()).add(shaman);
        return shaman;
    }

    private Permanent addReadyAttacker() {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage → graveyard trigger onto stack
        harness.passBothPriorities(); // resolve the trigger → may-pay prompt
    }

    @Test
    @DisplayName("Paying {3} returns the Shaman from the graveyard to hand")
    void payingReturnsShamanToHand() {
        Card shaman = putShamanInGraveyard();
        addReadyAttacker();
        harness.setLife(player2, 20);

        runCombatDamage();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the return-to-hand effect

        assertThat(gd.playerHands.get(player1.getId())).contains(shaman);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shaman);
    }

    @Test
    @DisplayName("Declining to pay leaves the Shaman in the graveyard")
    void decliningLeavesShamanInGraveyard() {
        Card shaman = putShamanInGraveyard();
        addReadyAttacker();
        harness.setLife(player2, 20);

        runCombatDamage();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shaman);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shaman);
    }

    @Test
    @DisplayName("Two attackers connecting still trigger the Shaman only once")
    void oneOrMoreCreaturesTriggersOnlyOnce() {
        putShamanInGraveyard();
        addReadyAttacker();
        addReadyAttacker();
        harness.setLife(player2, 20);

        runCombatDamage();

        // Two dealers, one "one or more creatures" trigger — a single pay-{3} prompt.
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodrush discards the Shaman and gives an attacking creature +3/+1")
    void bloodrushBoostsAttackingCreature() {
        Card shaman = new PyrewildShaman();
        harness.setHand(player1, List.of(shaman));
        Permanent attacker = addReadyAttacker();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shaman);
    }

    @Test
    @DisplayName("Bloodrush cannot target a creature that isn't attacking")
    void bloodrushRequiresAttackingCreature() {
        Card shaman = new PyrewildShaman();
        harness.setHand(player1, List.of(shaman));
        Permanent bystander = new Permanent(new GrizzlyBears());
        bystander.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bystander);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
