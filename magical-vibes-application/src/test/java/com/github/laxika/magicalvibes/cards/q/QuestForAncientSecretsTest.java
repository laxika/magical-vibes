package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestForAncientSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a quest counter on itself for every non-token card put into your graveyard")
    void gainsQuestCountersForCardsPutIntoOwnGraveyard() {
        Permanent quest = addQuest();
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears(), new Island(),
                new GrizzlyBears(), new Island()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveQuestMayAbilities(true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(6);
    }

    @Test
    @DisplayName("May decline leaves quest counters unchanged")
    void mayDeclineQuestCounter() {
        Permanent quest = addQuest();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveQuestMayAbilities(false);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removes five quest counters, sacrifices itself, and shuffles a target graveyard")
    void shufflesTargetGraveyard() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 5);
        harness.setGraveyard(player2, List.of(new Island(), new GrizzlyBears()));
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, indexOf(player1, quest), null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Quest for Ancient Secrets");
        harness.assertInGraveyard(player1, "Quest for Ancient Secrets");
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize + 2);
    }

    @Test
    @DisplayName("Cannot activate without five quest counters")
    void requiresFiveQuestCounters() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, quest), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent with the graveyard-shuffle ability")
    void requiresPlayerTarget() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 5);
        Permanent permanentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, quest), null,
                permanentTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(5);
    }

    private Permanent addQuest() {
        Permanent quest = harness.addToBattlefieldAndReturn(player1, new QuestForAncientSecrets());
        quest.setSummoningSick(false);
        return quest;
    }

    private void resolveQuestMayAbilities(boolean accept) {
        int guard = 0;
        while (guard++ < 100) {
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

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
