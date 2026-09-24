package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhoulcallerGisa.class, GrizzlyBears.class})
class GhoulcallerGisaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Zombie for each power of the sacrificed creature")
    void createsZombiesEqualToSacrificedPower() {
        addCreatureReady(player1, new GhoulcallerGisa());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    @DisplayName("Uses the sacrificed creature's effective power")
    void usesEffectivePower() {
        addCreatureReady(player1, new GhoulcallerGisa());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(3);
    }

    @Test
    @DisplayName("Cannot sacrifice Gisa herself")
    void cannotSacrificeSource() {
        addCreatureReady(player1, new GhoulcallerGisa());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
