package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.h.HatchingPlans;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillerInstinct.class, Gristleback.class, HatchingPlans.class})
class KillerInstinctTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep puts a revealed creature onto the battlefield with haste")
    void putsCreatureOntoBattlefieldWithHaste() {
        harness.addToBattlefield(player1, new KillerInstinct());
        harness.setLibrary(player1, List.of(new Gristleback()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Gristleback");
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Revealed creature is sacrificed at the next end step")
    void sacrificesCreatureAtNextEndStep() {
        harness.addToBattlefield(player1, new KillerInstinct());
        harness.setLibrary(player1, List.of(new Gristleback()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Gristleback")).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Gristleback")).isEmpty();
        harness.assertInGraveyard(player1, "Gristleback");
    }

    @Test
    @DisplayName("A revealed noncreature remains on top of the library")
    void leavesNoncreatureOnTop() {
        harness.addToBattlefield(player1, new KillerInstinct());
        HatchingPlans hatchingPlans = new HatchingPlans();
        harness.setLibrary(player1, List.of(hatchingPlans));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hatchingPlans);
        assertThat(findPermanents(player1, "Hatching Plans")).isEmpty();
    }

    @Test
    @DisplayName("An empty library produces no creature")
    void doesNothingWhenLibraryIsEmpty() {
        harness.addToBattlefield(player1, new KillerInstinct());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Killer Instinct");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
