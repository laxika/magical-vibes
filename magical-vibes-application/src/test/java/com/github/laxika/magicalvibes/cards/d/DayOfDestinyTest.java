package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DayOfDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary creatures you control get +2/+2")
    void buffsOwnLegendaryCreatures() {
        harness.addToBattlefield(player1, new DayOfDestiny());
        harness.addToBattlefield(player1, new AdelizTheCinderWind());

        Permanent adeliz = findPermanent(player1, "Adeliz, the Cinder Wind");
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff non-legendary creatures")
    void doesNotBuffNonLegendaryCreatures() {
        harness.addToBattlefield(player1, new DayOfDestiny());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's legendary creatures")
    void doesNotBuffOpponentLegendaryCreatures() {
        harness.addToBattlefield(player1, new DayOfDestiny());
        harness.addToBattlefield(player2, new AdelizTheCinderWind());

        Permanent adeliz = findPermanent(player2, "Adeliz, the Cinder Wind");
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Day of Destiny leaves the battlefield")
    void bonusRemovedWhenItLeaves() {
        harness.addToBattlefield(player1, new DayOfDestiny());
        harness.addToBattlefield(player1, new AdelizTheCinderWind());

        Permanent adeliz = findPermanent(player1, "Adeliz, the Cinder Wind");
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Day of Destiny"));

        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(2);
    }
}
