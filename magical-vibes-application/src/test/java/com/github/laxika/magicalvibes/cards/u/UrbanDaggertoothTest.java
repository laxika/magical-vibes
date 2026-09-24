package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrbanDaggertooth.class, GrizzlyBears.class, Shock.class})
class UrbanDaggertoothTest extends BaseCardTest {

    @Test
    @DisplayName("When dealt damage, proliferates")
    void proliferatesWhenDealtDamage() {
        harness.addToBattlefield(player2, new UrbanDaggertooth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        UUID daggertoothId = harness.getPermanentId(player2, "Urban Daggertooth");
        harness.castInstant(player1, 0, daggertoothId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Urban Daggertooth
        harness.passBothPriorities(); // Resolve the enrage trigger
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Urban Daggertooth")).isNotNull();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
