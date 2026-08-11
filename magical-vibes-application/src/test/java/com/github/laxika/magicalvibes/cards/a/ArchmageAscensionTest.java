package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchmageAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Two draws before an end step offer a quest counter")
    void twoDrawsOfferQuestCounter() {
        Permanent ascension = addAscension();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("One draw does not trigger the quest counter ability")
    void oneDrawDoesNotOfferQuestCounter() {
        Permanent ascension = addAscension();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));

        draw(player1);
        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Six quest counters offer a library search instead of a draw")
    void sixQuestCountersReplaceDrawWithSearch() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 6);
        harness.setHand(player1, List.of());
        GrizzlyBears bears = new GrizzlyBears();
        Island island = new Island();
        harness.setLibrary(player1, new ArrayList<>(List.of(island, bears)));

        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(island, bears);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Fewer than six quest counters allow a normal draw")
    void fewerThanSixQuestCountersDrawNormally() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 5);
        harness.setHand(player1, List.of());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears, new Island()));

        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The replacement affects only the enchantment's controller")
    void replacementOnlyAffectsController() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 6);
        harness.setHand(player2, List.of());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears, new Island()));

        draw(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        assertThat(gd.cardsDrawnThisTurn.get(player2.getId())).isEqualTo(1);
    }

    private Permanent addAscension() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new ArchmageAscension());
        ascension.setSummoningSick(false);
        return ascension;
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        if (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
