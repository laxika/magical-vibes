package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PurestrainGenestealer.class, Forest.class})
class PurestrainGenestealerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent genestealer = castGenestealer();

        assertThat(genestealer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting the attack trigger removes a counter and puts a basic land onto the battlefield tapped")
    void acceptsAttackTriggerAndSearchesForBasicLand() {
        Permanent genestealer = addGenestealerWithCounters(2);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(genestealer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent searchedForest = findPermanent(player1, "Forest");
        assertThat(searchedForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves counters and library unchanged")
    void decliningAttackTriggerDoesNothing() {
        Permanent genestealer = addGenestealerWithCounters(2);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(genestealer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the attack trigger without a counter does not search")
    void noCounterMeansNoSearch() {
        addGenestealerWithCounters(0);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Removing the last counter immediately searches only for basic lands")
    void lastCounterSearchesDuringTheSameResolution() {
        Permanent genestealer = addGenestealerWithCounters(1);
        Forest forest = new Forest();
        PurestrainGenestealer nonland = new PurestrainGenestealer();
        harness.setLibrary(player1, List.of(nonland, forest));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(genestealer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("The counter remains removed when the controller chooses not to find a land")
    void mayFailToFindAfterRemovingCounter() {
        Permanent genestealer = addGenestealerWithCounters(2);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(genestealer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(genestealer);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    private Permanent castGenestealer() {
        harness.setHand(player1, List.of(new PurestrainGenestealer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Purestrain Genestealer");
    }

    private Permanent addGenestealerWithCounters(int counters) {
        Permanent genestealer = addCreatureReady(player1, new PurestrainGenestealer());
        genestealer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return genestealer;
    }

    private void declareAttack() {
        declareAttackers(player1, List.of(0));
    }
}
