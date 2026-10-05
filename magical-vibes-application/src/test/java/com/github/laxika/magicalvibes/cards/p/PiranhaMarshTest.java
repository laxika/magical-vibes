package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PiranhaMarsh.class})
class PiranhaMarshTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and makes the chosen player lose 1 life")
    void entersTappedAndMakesChosenPlayerLoseLife() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PiranhaMarsh()));

        harness.playLand(player1, 0);

        Permanent marsh = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(marsh.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can target its controller with the enters-the-battlefield ability")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PiranhaMarsh()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Tapping adds one black mana")
    void tapsForBlackMana() {
        Permanent marsh = harness.addToBattlefieldAndReturn(player1, new PiranhaMarsh());
        marsh.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without being played still enters tapped and triggers life loss")
    void enteringWithoutBeingPlayedStillTriggers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent marsh = harness.enterBattlefieldAndReturn(player1, new PiranhaMarsh());

        assertThat(marsh.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A tapped Marsh cannot produce mana until untapped")
    void tappedMarshCannotProduceMana() {
        Permanent marsh = harness.addToBattlefieldAndReturn(player1, new PiranhaMarsh());
        marsh.setSummoningSick(false);
        marsh.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(marsh.isTapped()).isTrue();

        marsh.setTapped(false);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A noncreature land can produce mana even when marked summoning sick")
    void summoningSicknessDoesNotPreventManaAbility() {
        Permanent marsh = harness.addToBattlefieldAndReturn(player1, new PiranhaMarsh());
        marsh.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
