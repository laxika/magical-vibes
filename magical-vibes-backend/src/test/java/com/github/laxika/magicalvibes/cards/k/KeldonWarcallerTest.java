package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeldonWarcallerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK effect: PutLoreCounterOnTargetPermanentEffect")
    void hasOnAttackEffect() {
        KeldonWarcaller card = new KeldonWarcaller();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK))
                .hasSize(1)
                .allSatisfy(e -> {
                    assertThat(e).isInstanceOf(PutCounterOnTargetPermanentEffect.class);
                    assertThat(((PutCounterOnTargetPermanentEffect) e).counterType()).isEqualTo(CounterType.LORE);
                });
    }

    // ===== Attack trigger: put lore counter on target Saga =====

    @Test
    @DisplayName("Attacking with Keldon Warcaller queues target selection for a Saga")
    void attackTriggerQueuesForTargetSelection() {
        addWarcallerReady(player1);
        addSagaWithLoreCounters(player1, 0);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving attack trigger puts a lore counter on targeted Saga and triggers chapter ability")
    void attackTriggerPutsLoreCounterAndTriggersChapter() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addWarcallerReady(player1);
        Permanent saga = addSagaWithLoreCounters(player1, 0);

        declareAttackers(player1, List.of(0));

        // Choose the Saga as target
        harness.handlePermanentChosen(player1, saga.getId());

        // Resolve the attack trigger (puts lore counter)
        harness.passBothPriorities();

        // Saga should now have 1 lore counter
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));
    }

    @Test
    @DisplayName("Putting a lore counter on a Saga with 1 counter triggers chapter II")
    void loreCounterOnSagaWith1CounterTriggersChapterII() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addWarcallerReady(player1);
        Permanent saga = addSagaWithLoreCounters(player1, 1);

        declareAttackers(player1, List.of(0));

        // Choose the Saga as target
        harness.handlePermanentChosen(player1, saga.getId());

        // Resolve the attack trigger
        harness.passBothPriorities();

        // Saga should now have 2 lore counters
        assertThat(saga.getLoreCounters()).isEqualTo(2);

        // Chapter II ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));
    }

    @Test
    @DisplayName("Putting a lore counter on a Saga with 2 counters triggers chapter III")
    void loreCounterOnSagaWith2CountersTriggersChapterIII() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addWarcallerReady(player1);
        Permanent saga = addSagaWithLoreCounters(player1, 2);

        declareAttackers(player1, List.of(0));

        // Choose the Saga as target
        harness.handlePermanentChosen(player1, saga.getId());

        // Resolve the attack trigger
        harness.passBothPriorities();

        // Saga should now have 3 lore counters
        assertThat(saga.getLoreCounters()).isEqualTo(3);

        // Chapter III ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter III"));
    }

    @Test
    @DisplayName("Cannot target opponent's Saga")
    void cannotTargetOpponentsSaga() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addWarcallerReady(player1);
        Permanent opponentSaga = addSagaWithLoreCounters(player2, 0);

        // Need a valid target for the trigger to fire — add a Saga player1 controls
        Permanent ownSaga = addSagaWithLoreCounters(player1, 0);

        declareAttackers(player1, List.of(0));

        // Choosing opponent's Saga should fail
        assertThatThrownBy(
                () -> harness.handlePermanentChosen(player1, opponentSaga.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No trigger when no Sagas are controlled")
    void noTriggerWhenNoSagas() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addWarcallerReady(player1);

        declareAttackers(player1, List.of(0));

        // Should not be awaiting permanent choice since there are no valid targets
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Helpers =====

    private Permanent addWarcallerReady(Player player) {
        Permanent perm = new Permanent(new KeldonWarcaller());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSagaWithLoreCounters(Player player, int loreCounters) {
        Permanent saga = new Permanent(new HistoryOfBenalia());
        saga.setLoreCounters(loreCounters);
        gd.playerBattlefields.get(player.getId()).add(saga);
        return saga;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
