package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AfiyaGrove;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfWhisperedHopes.class, AfiyaGrove.class})
class KamiOfWhisperedHopesTest extends BaseCardTest {

    @Test
    @DisplayName("Adds mana equal to its power in the chosen color")
    void addsManaEqualToPower() {
        Permanent kami = addCreatureReady(player1, new KamiOfWhisperedHopes());
        kami.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds a +1/+1 counter to a noncreature permanent entering under its controller's control")
    void addsCounterToNoncreaturePermanent() {
        addCreatureReady(player1, new KamiOfWhisperedHopes());

        harness.setHand(player1, List.of(new AfiyaGrove()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent grove = findPermanent(player1, "Afiya Grove");
        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
