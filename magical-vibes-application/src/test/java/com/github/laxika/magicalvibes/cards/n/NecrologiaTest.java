package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Necrologia.class, GrizzlyBears.class, Counterspell.class})
class NecrologiaTest extends BaseCardTest {

    private void prepareCaster() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Necrologia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("Chooses and pays X as an additional cost while casting, then draws X cards")
    void payXLifeDrawXCards() {
        prepareCaster();
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, 3, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing X=0 as an additional cost draws nothing")
    void chooseZeroDoesNothing() {
        prepareCaster();
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, 0, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Countering Necrologia does not refund the life paid or draw cards")
    void counteringDoesNotRefundLife() {
        prepareCaster();
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        Necrologia spell = (Necrologia) harness.getGameData().playerHands.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 3, null);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertLife(player1, lifeBefore - 3);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Necrologia");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The announced X remains fixed when life changes before resolution")
    void lifeChangeDoesNotChangeCardsDrawn() {
        prepareCaster();

        harness.castInstant(player1, 0, 3, null);
        harness.setLife(player1, 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(3);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose X greater than the caster's life total while casting")
    void cannotPayMoreLifeThanAvailable() {
        prepareCaster();
        int life = harness.getGameData().playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, life + 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(life);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast outside your end step")
    void cannotCastOutsideEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Necrologia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast during an opponent's end step")
    void cannotCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Necrologia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
