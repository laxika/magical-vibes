package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SlickshotShowOff;
import com.github.laxika.magicalvibes.cards.t.TakeTheFall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanyonCrab.class, Forest.class, GrizzlyBears.class, Island.class, SlickshotShowOff.class, TakeTheFall.class})
class CanyonCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/-2 until end of turn")
    void getsBoostUntilEndOfTurn() {
        Permanent crab = addCrab();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crab.getEffectivePower()).isEqualTo(2);
        assertThat(crab.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crab.getEffectivePower()).isZero();
        assertThat(crab.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Draws then discards at the end step when no hand spell was cast")
    void drawsThenDiscardsWithoutHandSpell() {
        addCrab();
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw or discard after a spell was cast from hand")
    void doesNotLootAfterHandSpell() {
        addCrab();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void repeatedActivationsStackAndCanReduceToughnessToZero() {
        Permanent crab = addCrab();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Canyon Crab");
        harness.assertInGraveyard(player1, "Canyon Crab");
    }

    @Test
    void playingALandDoesNotPreventLooting() {
        addCrab();
        harness.setHand(player1, List.of(new Island()));
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.playLand(player1, 0);
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        addCrab();
        Island held = new Island();
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of(held));
        harness.setLibrary(player1, List.of(libraryCard));

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void castingCrabItselfFromHandPreventsLooting() {
        harness.setHand(player1, List.of(new CanyonCrab()));
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToEndStep(player1);

        harness.assertOnBattlefield(player1, "Canyon Crab");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void castingFromExileDoesNotPreventLooting() {
        SlickshotShowOff plotted = new SlickshotShowOff();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(plotted));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        addCrab();
        Island held = new Island();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(held));
        harness.setLibrary(player1, List.of(drawn));

        harness.castFromExile(player1, plotted.getId());
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held, drawn);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void castingFromHandInResponseStopsLootingAtResolution() {
        Permanent crab = addCrab();
        harness.setHand(player1, List.of(new TakeTheFall()));
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, crab.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCrab() {
        return harness.addToBattlefieldAndReturn(player1, new CanyonCrab());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
