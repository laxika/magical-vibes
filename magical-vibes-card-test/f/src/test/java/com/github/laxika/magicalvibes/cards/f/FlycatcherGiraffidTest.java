package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        harness.castFromHand(player1, new FlycatcherGiraffid(), "{4}{G}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("reach", "vigilance");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Flycatcher Giraffid");
    }

    @ParameterizedTest
    @ValueSource(strings = {"reach", "vigilance"})
    void entersWithChosenCounterWithoutBeingCast(String counterType) {
        Permanent giraffid = harness.addToBattlefieldAndReturn(player2, new FlycatcherGiraffid());
        harness.inMutationScope(() -> harness.getBattlefieldEntryService().handleCreatureEnteredBattlefield(
                gd, player2.getId(), giraffid.getCard(), null, false));

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("reach", "vigilance");
        harness.handleListChoice(player2, counterType);

        CounterType chosen = counterType.equals("reach") ? CounterType.REACH : CounterType.VIGILANCE;
        CounterType other = counterType.equals("reach") ? CounterType.VIGILANCE : CounterType.REACH;
        assertThat(giraffid.getCounterCount(chosen)).isEqualTo(1);
        assertThat(giraffid.getCounterCount(other)).isZero();
        assertThat(giraffid.hasKeyword(counterType.equals("reach") ? Keyword.REACH : Keyword.VIGILANCE))
                .isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
