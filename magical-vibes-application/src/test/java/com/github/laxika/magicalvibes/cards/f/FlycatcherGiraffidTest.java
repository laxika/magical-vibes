package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed(FlycatcherGiraffid.class)
class FlycatcherGiraffidTest extends BaseCardTest {

    @Test
    void entersWithReachCounterWhenChosen() {
        Permanent giraffid = castAndChoose("reach");

        assertThat(giraffid.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(giraffid.getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(giraffid.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(giraffid.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void entersWithVigilanceCounterWhenChosen() {
        Permanent giraffid = castAndChoose("vigilance");

        assertThat(giraffid.getCounterCount(CounterType.REACH)).isZero();
        assertThat(giraffid.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(giraffid.hasKeyword(Keyword.REACH)).isFalse();
        assertThat(giraffid.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    private Permanent castAndChoose(String counterType) {
        harness.setHand(player1, List.of(new FlycatcherGiraffid()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("reach", "vigilance");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Flycatcher Giraffid");
    }
}
