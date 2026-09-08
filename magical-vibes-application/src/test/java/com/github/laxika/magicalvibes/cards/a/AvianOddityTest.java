package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AvianOddity.class, GrizzlyBears.class})
class AvianOddityTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling puts a flying counter on a creature you control and draws a card")
    void cyclingPutsFlyingCounterOnOwnCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AvianOddity()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(target.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Avian Oddity");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling with no legal creature you control still draws a card")
    void cyclingWithoutLegalTargetStillDraws() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AvianOddity()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Avian Oddity");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling cannot target an opponent's creature")
    void cyclingCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AvianOddity()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
