package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestForTheHolyRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell offers a quest counter")
    void creatureSpellOffersQuestCounter() {
        Permanent quest = addQuest();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the creature spell trigger adds no quest counter")
    void decliningCreatureSpellTriggerAddsNoCounter() {
        Permanent quest = addQuest();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Casting a noncreature spell does not offer a quest counter")
    void noncreatureSpellDoesNotTrigger() {
        Permanent quest = addQuest();
        prepareMainPhase();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Quest for the Holy Relic"));
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing five quest counters and sacrificing searches for and attaches an Equipment")
    void removesCountersSacrificesAndAttachesEquipment() {
        Permanent quest = addQuest();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        quest.setCounterCount(CounterType.QUEST, 5);
        harness.setLibrary(player1, List.of(new StriderHarness()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(quest);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
        Permanent equipment = findPermanent(player1, "Strider Harness");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The ability cannot be activated without five quest counters")
    void cannotActivateWithoutFiveQuestCounters() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addQuest() {
        return harness.addToBattlefieldAndReturn(player1, new QuestForTheHolyRelic());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
