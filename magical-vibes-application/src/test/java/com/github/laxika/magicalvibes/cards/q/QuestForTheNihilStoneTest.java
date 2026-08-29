package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuestForTheNihilStoneTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent discarding a card may add a quest counter")
    void opponentDiscardAddsQuestCounter() {
        Permanent quest = harness.addToBattlefieldAndReturn(player1, new QuestForTheNihilStone());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two quest counters may make an empty-handed opponent lose 5 life")
    void emptyHandedOpponentLosesFiveLife() {
        Permanent quest = addQuestWithCounters(2);
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(2);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger below two quest counters")
    void upkeepAbilityRequiresTwoQuestCounters() {
        addQuestWithCounters(1);
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger while the opponent has cards in hand")
    void upkeepAbilityRequiresEmptyHand() {
        addQuestWithCounters(2);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addQuestWithCounters(int count) {
        Permanent quest = harness.addToBattlefieldAndReturn(player1, new QuestForTheNihilStone());
        quest.setCounterCount(CounterType.QUEST, count);
        return quest;
    }

}
