package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestForPureFlameTest extends BaseCardTest {

    @Test
    @DisplayName("May put a quest counter on itself when your source damages an opponent")
    void gainsQuestCounterFromDamageToOpponent() {
        Permanent quest = addQuest();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveMayAbility(true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the damage trigger does not add a quest counter")
    void mayDeclineQuestCounter() {
        Permanent quest = addQuest();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveMayAbility(false);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing four counters and sacrificing it doubles damage this turn")
    void activatesAndDoublesDamageThisTurn() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 4);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player1, "Quest for Pure Flame");
        harness.assertInGraveyard(player1, "Quest for Pure Flame");
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Cannot activate without four quest counters")
    void requiresFourQuestCounters() {
        addQuest().setCounterCount(CounterType.QUEST, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addQuest() {
        return harness.addToBattlefieldAndReturn(player1, new QuestForPureFlame());
    }

    private void resolveMayAbility(boolean accept) {
        int guard = 0;
        while (guard++ < 20) {
            PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(
                    PendingInteraction.MayAbilityChoice.class);
            if (choice != null) {
                harness.handleMayAbilityChosen(player1, accept);
            } else if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            } else {
                return;
            }
        }
        throw new IllegalStateException("Quest trigger resolution did not finish");
    }
}
