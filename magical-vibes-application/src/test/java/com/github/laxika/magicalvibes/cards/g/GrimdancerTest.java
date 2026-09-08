package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed(Grimdancer.class)
class GrimdancerTest extends BaseCardTest {

    @Test
    void choosesTwoDifferentCounters() {
        castGrimdancer();

        chooseCounter("menace");
        PendingInteraction.ColorChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.options()).containsExactly("deathtouch", "lifelink");

        harness.handleListChoice(player1, "deathtouch");

        Permanent grimdancer = findPermanent(player1, "Grimdancer");
        assertThat(grimdancer.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(grimdancer.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(grimdancer.getCounterCount(CounterType.LIFELINK)).isZero();
        assertThat(gqs.hasKeyword(gd, grimdancer, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, grimdancer, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void canChooseLifelinkAndDeathtouch() {
        castGrimdancer();

        chooseCounter("lifelink");
        PendingInteraction.ColorChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.options()).containsExactly("menace", "deathtouch");

        harness.handleListChoice(player1, "deathtouch");

        Permanent grimdancer = findPermanent(player1, "Grimdancer");
        assertThat(grimdancer.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(grimdancer.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, grimdancer, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, grimdancer, Keyword.MENACE)).isFalse();
    }

    private void castGrimdancer() {
        harness.setHand(player1, List.of(new Grimdancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("menace", "deathtouch", "lifelink");
    }

    private void chooseCounter(String counterType) {
        harness.handleListChoice(player1, counterType);
    }
}
