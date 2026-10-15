package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BraveTheElements;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.cards.s.SpidersilkNet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuestForTheHolyRelic.class, StoneworkPuma.class, BraveTheElements.class, SpidersilkNet.class})
class QuestForTheHolyRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell offers a quest counter")
    void creatureSpellOffersQuestCounter() {
        Permanent quest = addQuest();
        prepareMainPhase();
        harness.setHand(player1, List.of(new StoneworkPuma()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

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
        harness.setHand(player1, List.of(new StoneworkPuma()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

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
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Quest for the Holy Relic"));
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing five quest counters and sacrificing searches for and attaches an Equipment")
    void removesCountersSacrificesAndAttachesEquipment() {
        Permanent quest = addQuest();
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        quest.setCounterCount(CounterType.QUEST, 5);
        harness.setLibrary(player1, List.of(new SpidersilkNet()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(quest);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
        Permanent equipment = findPermanent(player1, "Spidersilk Net");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The ability cannot be activated without five quest counters")
    void cannotActivateWithoutFiveQuestCounters() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Quest for the Holy Relic");
    }

    @Test
    void equipmentEntersUnattachedWhenNoCreatureIsControlled() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 5);
        addCreatureReady(player2, new StoneworkPuma());
        harness.setLibrary(player1, List.of(new SpidersilkNet()));

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Quest for the Holy Relic");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Spidersilk Net").getAttachedTo()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayFailToFindEquipmentEvenWhenPresent() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 5);
        SpidersilkNet equipment = new SpidersilkNet();
        harness.setLibrary(player1, List.of(equipment));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Quest for the Holy Relic");
        harness.assertNotOnBattlefield(player1, "Spidersilk Net");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchWithNoEquipmentFinishesWithoutAttachment() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 5);
        StoneworkPuma creature = new StoneworkPuma();
        harness.setLibrary(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Quest for the Holy Relic");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsCreatureSpellDoesNotTriggerQuest() {
        Permanent quest = addQuest();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new StoneworkPuma()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Quest for the Holy Relic"));
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void libraryIsShuffledAfterEquipmentIsAttached() {
        Permanent quest = addQuest();
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        quest.setCounterCount(CounterType.QUEST, 5);
        harness.setLibrary(player1, List.of(new SpidersilkNet(), new StoneworkPuma()));
        gd.gameLog.clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Spidersilk Net").getAttachedTo()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isFalse();

        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Spidersilk Net").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gameLogContains("Library is shuffled")).isTrue();
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
