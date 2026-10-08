package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WydwenTheBitingGale.class})
class WydwenTheBitingGaleTest extends BaseCardTest {

    @Test
    @DisplayName("{U}{B}, Pay 1 life returns Wydwen to owner's hand")
    void activateReturnsToHand() {
        harness.addToBattlefield(player1, new WydwenTheBitingGale());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertInHand(player1, "Wydwen, the Biting Gale");
        harness.assertNotOnBattlefield(player1, "Wydwen, the Biting Gale");
    }

    @Test
    @DisplayName("Cannot activate ability with no life to pay")
    void cannotActivateWithoutLife() {
        harness.addToBattlefield(player1, new WydwenTheBitingGale());
        harness.setLife(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life is paid on activation, before Wydwen returns")
    void lifeIsPaidBeforeResolution() {
        harness.addToBattlefieldAndReturn(player1, new WydwenTheBitingGale()).setTapped(true);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 19);
        harness.assertOnBattlefield(player1, "Wydwen, the Biting Gale");
        harness.assertNotInHand(player1, "Wydwen, the Biting Gale");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Wydwen, the Biting Gale");
    }

    @Test
    @DisplayName("Both colored mana are required to activate")
    void cannotSubstituteBlueManaForBlack() {
        harness.addToBattlefield(player1, new WydwenTheBitingGale());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Wydwen, the Biting Gale");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Repeated activations pay separately and return only one card")
    void repeatedActivationsDoNotDuplicateCard() {
        harness.addToBattlefield(player1, new WydwenTheBitingGale());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 18);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Wydwen, the Biting Gale");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Wydwen, the Biting Gale");
    }

    @Test
    @DisplayName("Flash permits casting during the opponent's end step")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new WydwenTheBitingGale()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wydwen, the Biting Gale");
        harness.assertNotInHand(player1, "Wydwen, the Biting Gale");
    }

    @Test
    @DisplayName("A borrowed Wydwen returns to its owner while its controller pays")
    void returnsToOwnerRatherThanController() {
        WydwenTheBitingGale wydwen = new WydwenTheBitingGale();
        wydwen.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, wydwen);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Wydwen, the Biting Gale");
        harness.assertNotInHand(player1, "Wydwen, the Biting Gale");
        harness.assertNotOnBattlefield(player1, "Wydwen, the Biting Gale");
    }
}
