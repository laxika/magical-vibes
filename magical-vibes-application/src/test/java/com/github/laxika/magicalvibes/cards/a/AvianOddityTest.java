package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvianOddity.class, AlmightyBrushwagg.class})
class AvianOddityTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling puts a flying counter on a creature you control and draws a card")
    void cyclingPutsFlyingCounterOnOwnCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new AvianOddity()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(target.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Avian Oddity");
        harness.assertNotInHand(player1, "Almighty Brushwagg");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Cycling with no legal creature you control still draws a card")
    void cyclingWithoutLegalTargetStillDraws() {
        harness.addToBattlefield(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new AvianOddity()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Avian Oddity");
        harness.assertInHand(player1, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Cycling cannot target an opponent's creature")
    void cyclingCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new AvianOddity()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        addCyclingMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing the flying-counter target does not prevent the cycling draw")
    void losingTargetDoesNotPreventCyclingDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new AvianOddity()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Almighty Brushwagg");
        harness.assertInGraveyard(player1, "Avian Oddity");
        assertThat(target.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    @DisplayName("A legal creature must receive the mandatory cycling trigger")
    void cannotDeclineFlyingCounterWhenLegalCreatureExists() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new AvianOddity()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        harness.assertInHand(player1, "Almighty Brushwagg");
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
