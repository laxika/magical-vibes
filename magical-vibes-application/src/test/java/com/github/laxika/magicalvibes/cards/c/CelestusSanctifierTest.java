package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestusSanctifier.class})
class CelestusSanctifierTest extends BaseCardTest {

    @Test
    @DisplayName("Makes it day as it enters when there is no day/night designation")
    void makesItDayAsItEnters() {
        harness.castFromHand(player1, new CelestusSanctifier(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    @DisplayName("When day becomes night, puts one of the top two cards into the graveyard")
    void triggersWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new CelestusSanctifier());
        Card first = new CelestusSanctifier();
        Card second = new CelestusSanctifier();
        harness.setLibrary(player1, List.of(first, second));
        gd.spellsCastLastTurn.put(player2.getId(), 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
    }

    @Test
    void establishingDayDoesNotTriggerLibraryAbility() {
        Card first = new CelestusSanctifier();
        Card second = new CelestusSanctifier();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new CelestusSanctifier(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringDuringNightDoesNotChangeDesignationOrTrigger() {
        gd.dayNight = DayNight.NIGHT;
        Card top = new CelestusSanctifier();
        harness.setLibrary(player1, List.of(top));

        harness.castFromHand(player1, new CelestusSanctifier(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringDuringDayDoesNotTrigger() {
        gd.dayNight = DayNight.DAY;
        Card top = new CelestusSanctifier();
        harness.setLibrary(player1, List.of(top));

        harness.castFromHand(player1, new CelestusSanctifier(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void nightBecomesDayCanChooseSecondCardAndPreservesRestOfLibrary() {
        gd.dayNight = DayNight.NIGHT;
        harness.addToBattlefield(player2, new CelestusSanctifier());
        Card first = new CelestusSanctifier();
        Card second = new CelestusSanctifier();
        Card third = new CelestusSanctifier();
        Card opponentTop = new CelestusSanctifier();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(opponentTop));
        gd.recordSpellCast(player1.getId(), new CelestusSanctifier());
        gd.recordSpellCast(player1.getId(), new CelestusSanctifier());

        advanceToDayNightTrigger();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleCardChosen(player2, 1));

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, third);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void oneCardLibraryPutsOnlyCardIntoGraveyard() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new CelestusSanctifier());
        Card only = new CelestusSanctifier();
        harness.setLibrary(player1, List.of(only));

        advanceToDayNightTrigger();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(only);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryResolvesWithoutMovingCards() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new CelestusSanctifier());
        harness.setLibrary(player1, List.of());

        advanceToDayNightTrigger();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToDayNightTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
