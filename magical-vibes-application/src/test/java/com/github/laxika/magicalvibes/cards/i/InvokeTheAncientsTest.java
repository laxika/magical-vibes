package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InvokeTheAncients.class)
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

    private void cast() {
        harness.setHand(player1, List.of(new InvokeTheAncients()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
