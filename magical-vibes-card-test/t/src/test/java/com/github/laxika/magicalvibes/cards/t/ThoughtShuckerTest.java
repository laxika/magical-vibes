package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtShucker.class, GrizzlyBears.class, Shock.class})
class ThoughtShuckerTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold ability puts a +1/+1 counter on Thought Shucker and draws a card")
    void thresholdAbilityPutsCounterAndDrawsCard() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent thoughtShucker = addCreatureReady(player1, new ThoughtShucker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thoughtShucker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Threshold ability requires seven cards in its controller's graveyard")
    void thresholdAbilityRequiresSevenGraveyardCards() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addToBattlefield(player1, new ThoughtShucker());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    @Test
    @DisplayName("Threshold ability can be activated only once")
    void thresholdAbilityCanBeActivatedOnlyOnce() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent thoughtShucker = addCreatureReady(player1, new ThoughtShucker());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thoughtShucker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("activated only once");
    }

    @Test
    @DisplayName("Opponent's graveyard does not satisfy threshold")
    void opponentsGraveyardDoesNotSatisfyThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addToBattlefield(player1, new ThoughtShucker());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    @Test
    @DisplayName("Losing threshold after activation does not stop the counter or draw")
    void losingThresholdAfterActivationDoesNotStopResolution() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent thoughtShucker = harness.addToBattlefieldAndReturn(player1, new ThoughtShucker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(thoughtShucker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The once-only limit applies before the first activation resolves")
    void cannotActivateAgainWhileFirstActivationIsOnStack() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent thoughtShucker = harness.addToBattlefieldAndReturn(player1, new ThoughtShucker());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("activated only once");

        harness.passBothPriorities();
        assertThat(thoughtShucker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Thought Shucker can activate once, even while tapped and summoning sick")
    void eachPermanentHasItsOwnActivationLimit() {
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThoughtShucker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ThoughtShucker());
        first.tap();
        second.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
