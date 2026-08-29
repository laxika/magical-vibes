package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HiddenStockpileTest extends BaseCardTest {

    @Test
    @DisplayName("Revolt creates a Servo at the beginning of your end step")
    void revoltCreatesServo() {
        Permanent stockpile = harness.addToBattlefieldAndReturn(player1, new HiddenStockpile());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        sacrificeCreatureWithStockpile(stockpile, bears);
        resolveScryKeepingTopCard();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countServoTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Servo without revolt")
    void noServoWithoutRevolt() {
        harness.addToBattlefield(player1, new HiddenStockpile());

        advanceToEndStep(player1);

        assertThat(countServoTokens(player1)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice a creature and pay one mana to scry 1")
    void sacrificesCreatureAndScries() {
        Permanent stockpile = harness.addToBattlefieldAndReturn(player1, new HiddenStockpile());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        sacrificeCreatureWithStockpile(stockpile, bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private void resolveScryKeepingTopCard() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private void sacrificeCreatureWithStockpile(Permanent stockpile, Permanent creature) {
        int stockpileIndex = gd.playerBattlefields.get(player1.getId()).indexOf(stockpile);
        harness.activateAbility(player1, stockpileIndex, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private long countServoTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(Permanent::getCard)
                .filter(Card::isToken)
                .filter(card -> card.getName().equals("Servo"))
                .count();
    }
}
