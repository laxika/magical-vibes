package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IgneousCur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlpineHoundmaster.class, AlpineWatchdog.class, IgneousCur.class})
class AlpineHoundmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the enters-the-battlefield ability finds both named cards")
    void findsBothNamedCards() {
        castHoundmaster();
        harness.setLibrary(player1, List.of(
                new AlpineWatchdog(), new IgneousCur(), new AlpineHoundmaster()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Alpine Watchdog", "Igneous Cur");
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().requireDifferentNames()).isTrue();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Alpine Watchdog");
        harness.assertInHand(player1, "Igneous Cur");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The enters-the-battlefield search may be declined")
    void mayDeclineSearch() {
        castHoundmaster();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking gives +X/+0 for the other attacking creatures and wears off at end of turn")
    void boostsForOtherAttackersUntilEndOfTurn() {
        Permanent houndmaster = addCreatureReady(player1, new AlpineHoundmaster());
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new IgneousCur());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(houndmaster.getEffectivePower()).isEqualTo(4);
        assertThat(houndmaster.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(houndmaster.getEffectivePower()).isEqualTo(2);
        assertThat(houndmaster.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The search cannot find two copies of the same named card")
    void findsOnlyOneOfEachName() {
        castHoundmaster();
        harness.setLibrary(player1, List.of(
                new AlpineWatchdog(), new AlpineWatchdog(), new IgneousCur(), new IgneousCur()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsOnly("Igneous Cur");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Alpine Watchdog", "Igneous Cur");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Alpine Watchdog", "Igneous Cur");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may stop after finding only one of the two named cards")
    void mayFindOnlyOneCard() {
        castHoundmaster();
        harness.setLibrary(player1, List.of(new IgneousCur(), new AlpineWatchdog()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Igneous Cur");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Alpine Watchdog");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find even when both named cards are present")
    void mayFindNoCards() {
        castHoundmaster();
        harness.setLibrary(player1, List.of(new AlpineWatchdog(), new IgneousCur()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search finishes normally when neither named card is in the library")
    void noMatchingCards() {
        castHoundmaster();
        harness.setLibrary(player1, List.of(new AlpineHoundmaster()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Alpine Houndmaster");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Attacking alone gives no bonus even when other creatures are on the battlefield")
    void excludesSelfAndNonattackers() {
        Permanent houndmaster = addCreatureReady(player1, new AlpineHoundmaster());
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player2, new IgneousCur());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(houndmaster.getEffectivePower()).isEqualTo(2);
        assertThat(houndmaster.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus counts other attackers at resolution and stays fixed afterward")
    void countsAttackersAtResolution() {
        Permanent houndmaster = addCreatureReady(player1, new AlpineHoundmaster());
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());
        Permanent cur = addCreatureReady(player1, new IgneousCur());

        declareAttackers(List.of(0, 1, 2));
        watchdog.setAttacking(false);
        harness.passBothPriorities();

        assertThat(houndmaster.getEffectivePower()).isEqualTo(3);
        assertThat(houndmaster.getEffectiveToughness()).isEqualTo(2);

        cur.setAttacking(false);

        assertThat(houndmaster.getEffectivePower()).isEqualTo(3);
    }

    private void castHoundmaster() {
        harness.setHand(player1, List.of(new AlpineHoundmaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }
}
