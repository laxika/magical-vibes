package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FamilysFavor.class, GrizzlyBears.class, Forest.class})
class FamilysFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, it targets an attacking creature and gives it a shield counter")
    void targetsAttackingCreatureAndAddsShieldCounter() {
        harness.addToBattlefieldAndReturn(player1, new FamilysFavor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAttackTrigger();

        assertThat(attacker.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    @DisplayName("A shielded attacker draws when its combat damage removes the shield counter")
    void drawsWhenCombatDamageRemovesShieldCounter() {
        harness.addToBattlefieldAndReturn(player1, new FamilysFavor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAttackTrigger();

        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The granted combat-damage trigger expires at end of turn")
    void grantedTriggerExpiresAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new FamilysFavor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAttackTrigger();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        attacker.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    private void resolveAttackTrigger() {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
    }
}
