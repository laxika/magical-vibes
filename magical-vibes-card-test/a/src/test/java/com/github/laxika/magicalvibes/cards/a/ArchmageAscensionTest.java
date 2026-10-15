package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.cards.s.SagesOfTheAnima;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchmageAscension.class, StoneworkPuma.class, Island.class, SagesOfTheAnima.class})
class ArchmageAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Two draws before an end step offer a quest counter")
    void twoDrawsOfferQuestCounter() {
        Permanent ascension = addAscension();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StoneworkPuma(), new Island(), new StoneworkPuma()));

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
        harness.setLibrary(player1, List.of(new StoneworkPuma(), new Island()));

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
        StoneworkPuma puma = new StoneworkPuma();
        Island island = new Island();
        harness.setLibrary(player1, new ArrayList<>(List.of(island, puma)));

        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(island, puma);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(puma);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Fewer than six quest counters allow a normal draw")
    void fewerThanSixQuestCountersDrawNormally() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 5);
        harness.setHand(player1, List.of());
        StoneworkPuma puma = new StoneworkPuma();
        harness.setLibrary(player1, List.of(puma, new Island()));

        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(puma);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The replacement affects only the enchantment's controller")
    void replacementOnlyAffectsController() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 6);
        harness.setHand(player2, List.of());
        StoneworkPuma puma = new StoneworkPuma();
        harness.setLibrary(player2, List.of(puma, new Island()));

        draw(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(puma);
        assertThat(gd.cardsDrawnThisTurn.get(player2.getId())).isEqualTo(1);
    }

    @Test
    void mayDeclineQuestCounter() {
        Permanent ascension = addAscension();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        draw(player1);
        draw(player1);
        advanceToEndStep(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void mayDeclineReplacementAndDrawNormally() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 7);
        Island island = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(island));
        draw(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void emptyLibrarySearchReplacesDrawWithoutLosing() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 6);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        draw(player1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void eachDrawInMultipleDrawsCanBeReplacedIndependently() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 6);
        Island first = new Island();
        Island second = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2);
            harness.getPlayerInputService().processNextMayAbility(gd);
        });

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @CardUsed({ArchmageAscension.class, StoneworkPuma.class, SagesOfTheAnima.class})
    void controllerChoosesBetweenCompetingDrawReplacements() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 6);
        harness.addToBattlefield(player1, new SagesOfTheAnima());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StoneworkPuma()));

        draw(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInHand(player1, "Stonework Puma");
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    private Permanent addAscension() {
        return harness.addToBattlefieldAndReturn(player1, new ArchmageAscension());
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
