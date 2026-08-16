package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IgneousCur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlpineHoundmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the enters-the-battlefield ability finds both named cards")
    void findsBothNamedCards() {
        castHoundmaster();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new AlpineWatchdog(), new IgneousCur(), new GrizzlyBears()));

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
        addCreatureReady(player1, new GrizzlyBears());

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

    private void castHoundmaster() {
        harness.setHand(player1, List.of(new AlpineHoundmaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }
}
