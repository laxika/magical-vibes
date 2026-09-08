package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({StromgaldSpy.class, ElvishRanger.class})
class StromgaldSpyTest extends BaseCardTest {

    private Permanent addAttacker() {
        return addCreatureReady(player1, new StromgaldSpy());
    }

    private void attackUnblocked() {
        declareAttackers(List.of(0));

        // Advance into the declare-blockers step (the defender has no blockers), firing the
        // "attacks and isn't blocked" trigger, then resolve it to present the may choice.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private boolean seesOpponentHand(List<String> messages) {
        return messages.stream()
                .anyMatch(m -> m.contains("\"opponentHand\"") && !m.contains("\"opponentHand\":[]"));
    }

    private boolean player1SeesOpponentHand() {
        harness.clearMessages();
        harness.publishState();
        return seesOpponentHand(harness.getConn1().getSentMessages());
    }

    private boolean player2SeesOpponentHand() {
        harness.clearMessages();
        harness.publishState();
        return seesOpponentHand(harness.getConn2().getSentMessages());
    }

    @Test
    @DisplayName("Accepting reveals the defending player's hand and the Spy deals no combat damage")
    void unblockedAcceptRevealsHandAndPreventsDamage() {
        harness.setHand(player1, List.of(new ElvishRanger()));
        harness.setHand(player2, List.of(new ElvishRanger(), new ElvishRanger()));
        Permanent attacker = addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(player1SeesOpponentHand()).isTrue();
        assertThat(player2SeesOpponentHand()).isFalse();

        // Combat damage step: the Spy assigns none, so the defender stays at 20.
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The reveal ends when the Spy leaves the battlefield")
    void revealEndsWhenSpyLeavesBattlefield() {
        harness.setHand(player2, List.of(new ElvishRanger(), new ElvishRanger()));
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
        harness.setHand(player2, List.of(new ElvishRanger(), new ElvishRanger()));
        Permanent attacker = addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(player1SeesOpponentHand()).isFalse();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        harness.setHand(player2, List.of(new ElvishRanger(), new ElvishRanger()));

        Permanent blocker = addCreatureReady(player2, new ElvishRanger());

        Permanent attacker = addAttacker();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(player1SeesOpponentHand()).isFalse();
    }
}
