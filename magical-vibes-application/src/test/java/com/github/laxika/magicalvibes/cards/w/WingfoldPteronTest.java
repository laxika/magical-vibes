package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed(WingfoldPteron.class)
class WingfoldPteronTest extends BaseCardTest {

    @Test
    void entersWithFlyingCounterWhenChosen() {
        Permanent pteron = castAndChoose("flying");

        assertThat(pteron.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(pteron.getCounterCount(CounterType.HEXPROOF)).isZero();
        assertThat(pteron.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(pteron.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void entersWithHexproofCounterWhenChosen() {
        Permanent pteron = castAndChoose("hexproof");

        assertThat(pteron.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(pteron.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(pteron.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(pteron.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    private Permanent castAndChoose(String counterType) {
        harness.setHand(player1, List.of(new WingfoldPteron()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("flying", "hexproof");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Wingfold Pteron");
    }
}
