package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HelicaGlider.class)
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

    private Permanent castAndChoose(String counterType) {
        harness.setHand(player1, List.of(new HelicaGlider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("flying", "first strike");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Helica Glider");
    }
}
