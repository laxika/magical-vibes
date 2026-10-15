package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvokeTheAncients.class})
class InvokeTheAncientsTest extends BaseCardTest {

    @Test
    void createsTwoSpiritsAndChoosesAKeywordCounterForEach() {
        cast();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        PendingInteraction.ColorChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstChoice.options()).containsExactly("reach", "vigilance", "trample");

        harness.handleListChoice(player1, "reach");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "trample");

        assertThat(spirits.get(0).getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(spirits.get(0).getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(spirits.get(1).getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(spirits.get(1).getCounterCount(CounterType.REACH)).isZero();
        assertThat(spirits).allSatisfy(spirit ->
                assertThat(spirit.getCounterCount(CounterType.VIGILANCE)).isZero());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canChooseVigilanceForBothSpirits() {
        cast();

        harness.handleListChoice(player1, "vigilance");
        harness.handleListChoice(player1, "vigilance");

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
            assertThat(spirit.getCounterCount(CounterType.REACH)).isZero();
            assertThat(spirit.getCounterCount(CounterType.TRAMPLE)).isZero();
            assertThat(spirit.hasKeyword(Keyword.VIGILANCE)).isTrue();
        });
        assertThat(countPermanents(player2, "Spirit")).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof InvokeTheAncients);
    }

    private void cast() {
        harness.castFromHand(player1, new InvokeTheAncients(), "{1}{G}{G}{G}{G}");

        harness.passBothPriorities();
    }
}
