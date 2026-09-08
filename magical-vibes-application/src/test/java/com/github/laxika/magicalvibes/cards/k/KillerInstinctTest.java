package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillerInstinct.class, GrizzlyBears.class, Forest.class})
class KillerInstinctTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep puts a revealed creature onto the battlefield with haste")
    void putsCreatureOntoBattlefieldWithHaste() {
        harness.addToBattlefield(player1, new KillerInstinct());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Revealed creature is sacrificed at the next end step")
    void sacrificesCreatureAtNextEndStep() {
        harness.addToBattlefield(player1, new KillerInstinct());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A revealed noncreature remains on top of the library")
    void leavesNoncreatureOnTop() {
        harness.addToBattlefield(player1, new KillerInstinct());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
