package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GoblinMatron;
import com.github.laxika.magicalvibes.cards.k.KingOfThePride;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UniversalAutomaton.class, KingOfThePride.class, GoblinMatron.class})
class UniversalAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling makes Universal Automaton a Cat")
    void changelingMakesUniversalAutomatonACat() {
        harness.addToBattlefield(player1, new KingOfThePride());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
    }

    @Test
    @DisplayName("Changeling lets Goblin Matron find Universal Automaton in the library")
    void changelingAllowsGoblinLibrarySearch() {
        UniversalAutomaton automaton = new UniversalAutomaton();
        KingOfThePride nonGoblin = new KingOfThePride();
        harness.setLibrary(player1, List.of(nonGoblin, automaton));
        harness.castFromHand(player1, new GoblinMatron(), "{2}{R}");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(automaton);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(automaton).doesNotContain(nonGoblin);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonGoblin);
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
