package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StromgaldSpyTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new StromgaldSpy());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private void attackUnblocked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Advance into the declare-blockers step (the defender has no blockers), firing the
        // "attacks and isn't blocked" trigger, then resolve it to present the may choice.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private boolean player1SeesOpponentHand() {
        harness.clearMessages();
        harness.publishState();
        return harness.getConn1().getSentMessages().stream()
                .anyMatch(m -> m.contains("\"opponentHand\"") && !m.contains("\"opponentHand\":[]"));
    }

    @Test
    @DisplayName("Accepting reveals the defending player's hand and the Spy deals no combat damage")
    void unblockedAcceptRevealsHandAndPreventsDamage() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        Permanent attacker = addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(player1SeesOpponentHand()).isTrue();

        // Combat damage step: the Spy assigns none, so the defender stays at 20.
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The reveal ends when the Spy leaves the battlefield")
    void revealEndsWhenSpyLeavesBattlefield() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        Permanent attacker = addAttacker();

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(player1SeesOpponentHand()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(attacker);

        assertThat(player1SeesOpponentHand()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves the hand hidden and the Spy deals its combat damage")
    void unblockedDeclineKeepsHandHidden() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        Permanent attacker = addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(player1SeesOpponentHand()).isFalse();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(player1SeesOpponentHand()).isFalse();
    }
}
