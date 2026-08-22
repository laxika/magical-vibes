package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenInterrupter.class, GrizzlyBears.class, ThinkTwice.class})
class AvenInterrupterTest extends BaseCardTest {

    private void exileAndPlotOpponentSpell(GrizzlyBears bears) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new AvenInterrupter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles and plots the target spell")
    void etbExilesAndPlotsTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        exileAndPlotOpponentSpell(bears);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId())
                .contains(bears.getId());
        assertThat(gd.plottedCardIds).contains(bears.getId());
    }

    @Test
    @DisplayName("A plotted spell cannot be cast until a later turn and is cast as a sorcery for free")
    void plottedSpellWaitsForLaterTurn() {
        GrizzlyBears bears = new GrizzlyBears();
        exileAndPlotOpponentSpell(bears);

        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getId()))
                .hasMessageContaining("on the turn it became plotted");

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 2); // Aven Interrupter taxes this exile cast.
        harness.castFromExile(player2, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Opponents' spells cast from graveyards cost {2} more")
    void taxesOpponentGraveyardSpell() {
        harness.addToBattlefield(player1, new AvenInterrupter());
        ThinkTwice thinkTwice = new ThinkTwice();
        harness.setGraveyard(player2, List.of(thinkTwice));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .hasMessageContaining("Not enough mana");

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castFlashback(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId())
                .contains(thinkTwice.getId());
    }
}
