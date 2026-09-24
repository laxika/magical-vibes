package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuntouchedMyr.class})
class SuntouchedMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst puts one +1/+1 counter on it for each color spent")
    void sunburstCountsDistinctColors() {
        harness.setHand(player1, List.of(new SuntouchedMyr()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent myr = findPermanent(player1, "Suntouched Myr");
        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sunburst counts a repeated color only once")
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new SuntouchedMyr()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent myr = findPermanent(player1, "Suntouched Myr");
        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sunburst ignores colorless mana")
    void sunburstIgnoresColorlessMana() {
        harness.setHand(player1, List.of(new SuntouchedMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent myr = findPermanent(player1, "Suntouched Myr");
        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
