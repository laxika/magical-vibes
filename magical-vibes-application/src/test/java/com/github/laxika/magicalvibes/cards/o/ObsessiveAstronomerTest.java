package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObsessiveAstronomer.class, Forest.class, Island.class, Mountain.class})
class ObsessiveAstronomerTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new ObsessiveAstronomer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardsUpToTwoThenDrawsThatManyWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new ObsessiveAstronomer());
        harness.setHand(player1, List.of(new Forest(), new Island()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        advanceToNextUpkeep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mountain", "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island");
    }

    @Test
    void triggersWhenNightBecomesDayAndMayDiscardZero() {
        gd.dayNight = DayNight.NIGHT;
        gd.recordSpellCast(player1.getId(), new ObsessiveAstronomer());
        gd.recordSpellCast(player1.getId(), new ObsessiveAstronomer());
        harness.addToBattlefield(player1, new ObsessiveAstronomer());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Mountain()));

        advanceToNextUpkeep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Island");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    void enteringAtNightDoesNotMakeItDayOrTriggerRummaging() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new ObsessiveAstronomer(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    void enteringDuringDayDoesNotTriggerRummaging() {
        gd.dayNight = DayNight.DAY;
        harness.setHand(player1, List.of(new ObsessiveAstronomer(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    void mayDiscardOnlyOneCardFromALargerHand() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new ObsessiveAstronomer());
        harness.setHand(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setLibrary(player1, List.of(new ObsessiveAstronomer(), new Forest()));

        advanceToNextUpkeep();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Mountain", "Obsessive Astronomer");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).containsExactly("Island");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyHandDoesNotDrawOrRequireAChoice() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new ObsessiveAstronomer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));

        advanceToNextUpkeep();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsSpellsDoNotMakeNightBecomeDay() {
        gd.dayNight = DayNight.NIGHT;
        gd.recordSpellCast(player2.getId(), new ObsessiveAstronomer());
        gd.recordSpellCast(player2.getId(), new ObsessiveAstronomer());
        harness.addToBattlefield(player1, new ObsessiveAstronomer());
        harness.setHand(player1, List.of(new Island()));

        advanceToNextUpkeep();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Island");
    }

    @Test
    void becomingDayForTheFirstTimeDoesNotTriggerRummaging() {
        harness.setHand(player1, List.of(new ObsessiveAstronomer(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    void controllerDiscardsAndDrawsWhenTheOtherPlayersTurnEnds() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player2, new ObsessiveAstronomer());
        harness.setHand(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Mountain(), new Forest()));

        advanceToNextUpkeep();
        harness.handleXValueChosen(player2, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Island");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToNextUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();
    }
}
