package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScryingGlassTest extends BaseCardTest {

    private Permanent addReadyGlass() {
        Permanent glass = harness.addToBattlefieldAndReturn(player1, new ScryingGlass());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return glass;
    }

    private void activate(Permanent glass) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(glass),
                0, null, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws when the chosen number of cards has the chosen color")
    void drawsOnExactNumberAndColorMatch() {
        harness.setHand(player2, List.of(new GoblinPiker(), new GoblinPiker(), new GrizzlyBears()));
        Permanent glass = addReadyGlass();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        activate(glass);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Does not draw when the chosen number does not match")
    void doesNotDrawOnNumberMismatch() {
        harness.setHand(player2, List.of(new GoblinPiker(), new GoblinPiker(), new GrizzlyBears()));
        Permanent glass = addReadyGlass();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        activate(glass);
        harness.handleXValueChosen(player1, 1);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetSelf() {
        Permanent glass = addReadyGlass();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(glass), 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
