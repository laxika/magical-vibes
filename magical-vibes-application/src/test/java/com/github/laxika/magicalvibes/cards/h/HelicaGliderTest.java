package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HelicaGlider.class})
class HelicaGliderTest extends BaseCardTest {

    @Test
    void entersWithFlyingCounterWhenChosen() {
        Permanent glider = castAndChoose("flying");

        assertThat(glider.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(glider.getCounterCount(CounterType.FIRST_STRIKE)).isZero();
        assertThat(glider.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(glider.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void entersWithFirstStrikeCounterWhenChosen() {
        Permanent glider = castAndChoose("first strike");

        assertThat(glider.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(glider.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(glider.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(glider.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void controllerChoosesCounterWhenEnteringWithoutBeingCast() {
        Permanent glider = harness.enterBattlefieldAndReturn(player2, new HelicaGlider());

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("flying", "first strike");
        harness.handleListChoice(player2, "flying");

        assertThat(glider.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(glider.getCounterCount(CounterType.FIRST_STRIKE)).isZero();
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castAndChoose(String counterType) {
        harness.castFromHand(player1, new HelicaGlider(), "{2}{W}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("flying", "first strike");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Helica Glider");
    }
}
