package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZuranEnchanter.class, BalduvianBears.class})
class ZuranEnchanterTest extends BaseCardTest {

    private Permanent readyEnchanter() {
        Permanent enchanter = addCreatureReady(player1, new ZuranEnchanter());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return enchanter;
    }

    @Test
    @DisplayName("Target opponent discards a card")
    void opponentDiscards() {
        harness.setHand(player2, List.of(new BalduvianBears(), new BalduvianBears()));
        Permanent enchanter = readyEnchanter();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 1);

        assertThat(enchanter.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Can target any player, including its controller")
    void controllerCanBeTargeted() {
        harness.setHand(player1, List.of(new BalduvianBears()));
        readyEnchanter();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Empty hand causes no discard")
    void emptyHandNoDiscard() {
        harness.setHand(player2, List.of());
        readyEnchanter();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot be activated during the opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.setHand(player2, List.of(new BalduvianBears()));
        addCreatureReady(player1, new ZuranEnchanter());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated when already tapped")
    void cannotActivateWhenTapped() {
        Permanent enchanter = readyEnchanter();
        enchanter.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can activate during its controller's end step")
    void canActivateDuringOwnEndStep() {
        harness.setHand(player2, List.of(new BalduvianBears()));
        readyEnchanter();
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Pays two generic mana and one black mana on activation")
    void paysExactActivationCost() {
        Permanent enchanter = readyEnchanter();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(enchanter.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void requiresBlackMana() {
        Permanent enchanter = readyEnchanter();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(enchanter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent enchanter = readyEnchanter();
        enchanter.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(enchanter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent enchanter = readyEnchanter();
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(enchanter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
