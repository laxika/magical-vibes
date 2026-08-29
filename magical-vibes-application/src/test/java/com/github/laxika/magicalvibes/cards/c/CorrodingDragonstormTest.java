package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorrodingDragonstorm.class, Forest.class, GrizzlyBears.class, Island.class, ShivanDragon.class})
class CorrodingDragonstormTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent loses 2 life, you gain 2 life, and you surveil 2")
    void enteringDrainsAndSurveils() {
        Card milled = new Forest();
        Card kept = new Island();
        harness.setLibrary(player1, List.of(milled, kept));
        harness.setHand(player1, List.of(new CorrodingDragonstorm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(kept);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Returns to its owner's hand when a Dragon you control enters")
    void returnsWhenAllyDragonEnters() {
        harness.addToBattlefield(player1, new CorrodingDragonstorm());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Corroding Dragonstorm");
        harness.assertInHand(player1, "Corroding Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when a non-Dragon creature enters")
    void doesNotReturnForNonDragon() {
        harness.addToBattlefield(player1, new CorrodingDragonstorm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Corroding Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when an opponent's Dragon enters")
    void doesNotReturnForOpponentDragon() {
        harness.addToBattlefield(player1, new CorrodingDragonstorm());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ShivanDragon()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Corroding Dragonstorm");
    }
}
