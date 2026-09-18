package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaneOfProgress.class, GrizzlyBears.class, Ornithopter.class, RuleOfLaw.class})
class BaneOfProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts and enchantments and gets a counter for each")
    void destroysArtifactsAndEnchantmentsAndGetsCounters() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BaneOfProgress()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bane = findPermanent(player1, "Bane of Progress");
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy creatures or put counters on itself without matching permanents")
    void ignoresNonArtifactAndNonEnchantmentPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BaneOfProgress()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bane = findPermanent(player1, "Bane of Progress");
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
