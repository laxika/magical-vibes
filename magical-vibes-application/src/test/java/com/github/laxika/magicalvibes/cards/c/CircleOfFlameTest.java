package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CircleOfFlameTest extends BaseCardTest {

    /** Puts Circle of Flame on player1's battlefield and the given attacker on player2's. */
    private Permanent setUpAttack(Permanent attacker) {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new CircleOfFlame()));

        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        return attacker;
    }

    @Test
    @DisplayName("A non-flying attacker triggers Circle of Flame against that attacker")
    void nonFlyerTriggersAbility() {
        Permanent attacker = setUpAttack(new Permanent(new GrizzlyBears()));

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving the trigger deals 1 damage to the attacking creature")
    void dealsOneDamageToAttacker() {
        Permanent attacker = setUpAttack(new Permanent(new GrizzlyBears()));

        gs.declareAttackers(gd, player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A flying attacker does not trigger Circle of Flame")
    void flyerDoesNotTrigger() {
        Permanent attacker = setUpAttack(new Permanent(new AirElemental()));

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }
}
