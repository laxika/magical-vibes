package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.g.GoblinArsonist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuestForTheGoblinLord.class, GoblinArsonist.class, GrizzlyBears.class,
        BoggartShenanigans.class, Opalescence.class})
class QuestForTheGoblinLordTest extends BaseCardTest {

    @Test
    @DisplayName("A Goblin entering under your control may add a quest counter")
    void goblinEnteringAddsQuestCounter() {
        Permanent quest = addQuest();
        harness.castFromHand(player1, new GoblinArsonist(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Goblin entering under your control does not trigger the quest")
    void nonGoblinDoesNotAddQuestCounter() {
        Permanent quest = addQuest();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Five quest counters give your creatures +2/+0")
    void fiveQuestCountersBoostOwnCreatures() {
        Permanent quest = addQuest();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        quest.setCounterCount(CounterType.QUEST, 5);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The fifth quest counter turns on the creature boost")
    void fifthQuestCounterTurnsOnBoost() {
        Permanent quest = addQuest();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        quest.setCounterCount(CounterType.QUEST, 4);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);

        harness.castFromHand(player1, new GoblinArsonist(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("You may decline the quest counter")
    void mayDeclineQuestCounter() {
        Permanent quest = addQuest();
        harness.castFromHand(player1, new GoblinArsonist(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's Goblin does not trigger the quest")
    void opponentGoblinDoesNotTrigger() {
        Permanent quest = addQuest();
        harness.enterBattlefieldAndReturn(player2, new GoblinArsonist());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("A noncreature Goblin permanent entering may add a quest counter")
    void noncreatureGoblinEnteringAddsQuestCounter() {
        Permanent quest = addQuest();
        harness.castFromHand(player1, new BoggartShenanigans(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("An animated quest receives its own +2/+0 boost")
    void animatedQuestReceivesOwnBoost() {
        Permanent quest = addQuest();
        harness.addToBattlefield(player1, new Opalescence());
        quest.setCounterCount(CounterType.QUEST, 5);

        assertThat(gqs.getEffectivePower(gd, quest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, quest)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters above five keep the boost active and dropping below five removes it")
    void boostTracksCurrentQuestCounterThreshold() {
        Permanent quest = addQuest();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        quest.setCounterCount(CounterType.QUEST, 6);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        quest.setCounterCount(CounterType.QUEST, 4);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private Permanent addQuest() {
        return harness.addToBattlefieldAndReturn(player1, new QuestForTheGoblinLord());
    }
}
