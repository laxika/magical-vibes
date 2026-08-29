package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GravityWellTest extends BaseCardTest {

    private Permanent setUpAttack(Permanent attacker) {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GravityWell()));

        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        return attacker;
    }

    @Test
    @DisplayName("A creature with flying triggers Gravity Well when it attacks")
    void flyingCreatureTriggersAbility() {
        Permanent attacker = setUpAttack(new Permanent(new AirElemental()));

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving Gravity Well removes flying until end of turn")
    void removesFlyingFromAttackingCreature() {
        Permanent attacker = setUpAttack(new Permanent(new AirElemental()));

        gs.declareAttackers(gd, player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A creature without flying does not trigger Gravity Well")
    void nonFlyingCreatureDoesNotTrigger() {
        Permanent attacker = setUpAttack(new Permanent(new GrizzlyBears()));

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }
}
