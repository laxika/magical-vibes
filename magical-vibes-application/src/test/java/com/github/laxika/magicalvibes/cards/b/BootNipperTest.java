package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed(BootNipper.class)
class BootNipperTest extends BaseCardTest {

    @Test
    void entersWithDeathtouchCounterWhenChosen() {
        Permanent nipper = castAndChoose("deathtouch");

        assertThat(nipper.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(nipper.getCounterCount(CounterType.LIFELINK)).isZero();
        assertThat(nipper.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(nipper.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    void entersWithLifelinkCounterWhenChosen() {
        Permanent nipper = castAndChoose("lifelink");

        assertThat(nipper.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(nipper.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(nipper.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(nipper.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    private Permanent castAndChoose(String counterType) {
        harness.setHand(player1, List.of(new BootNipper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("deathtouch", "lifelink");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Boot Nipper");
    }
}
