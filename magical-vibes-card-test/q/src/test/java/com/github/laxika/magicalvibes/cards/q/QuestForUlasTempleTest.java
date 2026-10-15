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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuestForUlasTemple.class, GiantOctopus.class, GrizzlyBears.class, Forest.class})
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

    @Test
    @DisplayName("Looking at the top card is optional even when it is not a creature")
    void mayDeclineLookingAtNoncreature() {
        Permanent temple = addTemple();
        Card forest = new Forest();
        harness.setLibrary(player1, deckOf(forest, new GiantOctopus()));
        beginUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(temple.getCounterCount(CounterType.QUEST)).isZero();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Any creature can be revealed, even when it is not a sea creature")
    void nonSeaCreatureCanAddQuestCounter() {
        Permanent temple = addTemple();
        harness.setLibrary(player1, deckOf(new GrizzlyBears(), new Forest()));

        runUpkeep(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(temple.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller can decline the end-step creature placement")
    void mayDeclineSeaCreatureAtOwnEndStep() {
        Permanent temple = addTemple();
        temple.setCounterCount(CounterType.QUEST, 4);
        Card octopus = new GiantOctopus();
        harness.setHand(player1, List.of(octopus));

        runEndStep(player1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Giant Octopus");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(octopus);
        assertThat(temple.getCounterCount(CounterType.QUEST)).isEqualTo(4);
    }

    @Test
    @DisplayName("The end-step condition is checked again when the trigger resolves")
    void losingQuestCounterBeforeResolutionPreventsPlacement() {
        Permanent temple = addTemple();
        temple.setCounterCount(CounterType.QUEST, 3);
        Card octopus = new GiantOctopus();
        harness.setHand(player1, List.of(octopus));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        temple.setCounterCount(CounterType.QUEST, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(octopus);
        harness.assertNotOnBattlefield(player1, "Giant Octopus");
    }

    @Test
    @DisplayName("Each end-step trigger puts only one creature onto the battlefield without spending counters")
    void putsOnlyOneSeaCreatureAndKeepsCounters() {
        Permanent temple = addTemple();
        temple.setCounterCount(CounterType.QUEST, 3);
        Card first = new GiantOctopus();
        Card second = new GiantOctopus();
        harness.setHand(player1, List.of(first, second));

        runEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == first || permanent.getCard() == second)
                .hasSize(1);
        assertThat(temple.getCounterCount(CounterType.QUEST)).isEqualTo(3);
    }

    @Test
    @DisplayName("The top-card ability does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotOfferLookOrAddCounter() {
        Permanent temple = addTemple();
        Card octopus = new GiantOctopus();
        harness.setLibrary(player1, deckOf(octopus));
        harness.setLibrary(player2, deckOf(new Forest(), new Forest()));

        beginUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(octopus);
        assertThat(temple.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Accepting the end-step ability with no eligible card does not put another creature into play")
    void noEligibleSeaCreatureDoesNotOfferHandChoice() {
        Permanent temple = addTemple();
        temple.setCounterCount(CounterType.QUEST, 3);
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new Forest()));

        runEndStep(player2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(temple.getCounterCount(CounterType.QUEST)).isEqualTo(3);
    }

    private Permanent addTemple() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new QuestForUlasTemple());
        return temple;
    }

    private void runUpkeep(Player activePlayer) {
        beginUpkeep(activePlayer);
        harness.handleMayAbilityChosen(activePlayer, true);
    }

    private void beginUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
        harness.passBothPriorities();
    }

    private void runEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
