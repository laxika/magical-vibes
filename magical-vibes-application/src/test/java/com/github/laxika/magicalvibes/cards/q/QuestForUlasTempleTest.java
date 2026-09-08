package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuestForUlasTempleTest extends BaseCardTest {

    @Test
    @DisplayName("A creature on top may be revealed for a quest counter")
    void creatureTopAddsQuestCounterWhenAccepted() {
        Permanent temple = addTemple();
        Card octopus = new GiantOctopus();
        harness.setLibrary(player1, deckOf(octopus, new Forest()));

        runUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(octopus);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(temple.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        harness.assertInHand(player1, "Giant Octopus");
    }

    @Test
    @DisplayName("Declining the creature reveal leaves the top card and adds no counter")
    void decliningCreatureRevealAddsNoCounter() {
        Permanent temple = addTemple();
        Card octopus = new GiantOctopus();
        harness.setLibrary(player1, deckOf(octopus));

        runUpkeep(player1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(octopus);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(temple.getCounterCount(CounterType.QUEST)).isZero();
        harness.assertInHand(player1, "Giant Octopus");
    }

    @Test
    @DisplayName("A noncreature top card is not offered for reveal")
    void noncreatureTopCardDoesNotOfferReveal() {
        Permanent temple = addTemple();
        Card forest = new Forest();
        harness.setLibrary(player1, deckOf(forest, new GiantOctopus()));

        runUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(temple.getCounterCount(CounterType.QUEST)).isZero();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(GiantOctopus.class);
    }

    @Test
    @DisplayName("Three quest counters enable putting a sea creature from hand onto the battlefield")
    void putsSeaCreatureFromHandAtEndStep() {
        Permanent temple = addTemple();
        temple.setCounterCount(CounterType.QUEST, 3);
        Card octopus = new GiantOctopus();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(octopus, bears));

        runEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Giant Octopus");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("The end-step ability does not trigger below three quest counters")
    void endStepRequiresThreeQuestCounters() {
        Permanent temple = addTemple();
        temple.setCounterCount(CounterType.QUEST, 2);
        harness.setLibrary(player1, deckOf(new Forest()));

        runEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTemple() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new QuestForUlasTemple());
        temple.setSummoningSick(false);
        return temple;
    }

    private void runUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void runEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
