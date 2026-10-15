package com.github.laxika.magicalvibes.cards.n;

import java.util.List;
import java.util.stream.IntStream;

import com.github.laxika.magicalvibes.cards.b.Braingeyser;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightmaresAndDaydreams.class, DarkRitual.class, GrizzlyBears.class, Braingeyser.class})
class NightmaresAndDaydreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapters I through III make instant and sorcery spells mill their mana value")
    void chaptersMillBySpellManaValue() {
        addSagaWithLore(0);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DarkRitual()));

        advanceToNextChapter();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The chapter trigger does not fire for creature spells")
    void chaptersDoNotTriggerForCreatureSpells() {
        addSagaWithLore(0);
        advanceToNextChapter();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Chapter IV draws one card when no graveyard has twenty cards")
    void chapterIVDrawsOneBelowThreshold() {
        addSagaWithLore(3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Chapter IV draws three cards when any graveyard has twenty cards")
    void chapterIVDrawsThreeAtThreshold() {
        addSagaWithLore(3);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("The delayed chapter trigger expires when its controller's next turn begins")
    void delayedTriggerExpiresAtNextTurn() {
        addSagaWithLore(0);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        advanceToNextChapter();

        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Chapters II and III independently register milling triggers")
    void laterChaptersMill(int previousLore) {
        addSagaWithLore(previousLore);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        advanceToNextChapter();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sorcery mana value includes X and the controller can mill themselves")
    void sorceryIncludesXAndCanTargetController() {
        addSagaWithLore(0);
        harness.setLibrary(player1, IntStream.range(0, 8).mapToObj(i -> new GrizzlyBears()).toList());
        advanceToNextChapter();
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 3, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The delayed trigger fires for every matching spell")
    void repeatedCastsEachMill() {
        addSagaWithLore(0);
        advanceToNextChapter();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0);
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Opponent spells do not fire the controller's delayed trigger")
    void opponentsSpellsDoNotTrigger() {
        addSagaWithLore(0);
        advanceToNextChapter();
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The delayed trigger remains active during the opponent's turn")
    void delayedTriggerSurvivesTurnCleanup() {
        addSagaWithLore(0);
        advanceToNextChapter();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The delayed trigger remains after the Saga leaves the battlefield")
    void delayedTriggerSurvivesSourceRemoval() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, saga));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nightmares and Daydreams");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Chapter IV does not combine different players' graveyards")
    void chapterIVDoesNotCombineGraveyards() {
        addSagaWithLore(3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, IntStream.range(0, 10).mapToObj(i -> (Card) new GrizzlyBears()).toList());
        harness.setGraveyard(player2, IntStream.range(0, 19).mapToObj(i -> (Card) new GrizzlyBears()).toList());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertNotOnBattlefield(player1, "Nightmares and Daydreams");
        harness.assertInGraveyard(player1, "Nightmares and Daydreams");
    }

    @Test
    @DisplayName("Chapter I triggers as the Saga enters the battlefield")
    void enteringSagaRegistersFirstChapter() {
        harness.setHand(player1, List.of(new NightmaresAndDaydreams(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NightmaresAndDaydreams());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
