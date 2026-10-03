package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.y.YomijiWhoBarsTheWay;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DayOfDestiny.class, GnarledMass.class, YomijiWhoBarsTheWay.class, Opalescence.class})
class DayOfDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary creatures you control get +2/+2")
    void buffsOwnLegendaryCreatures() {
        harness.addToBattlefield(player1, new DayOfDestiny());
        Permanent yomiji = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());

        assertThat(gqs.getEffectivePower(gd, yomiji)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, yomiji)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not buff non-legendary creatures")
    void doesNotBuffNonLegendaryCreatures() {
        harness.addToBattlefield(player1, new DayOfDestiny());
        Permanent gnarledMass = harness.addToBattlefieldAndReturn(player1, new GnarledMass());

        assertThat(gqs.getEffectivePower(gd, gnarledMass)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gnarledMass)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's legendary creatures")
    void doesNotBuffOpponentLegendaryCreatures() {
        harness.addToBattlefield(player1, new DayOfDestiny());
        Permanent yomiji = harness.addToBattlefieldAndReturn(player2, new YomijiWhoBarsTheWay());

        assertThat(gqs.getEffectivePower(gd, yomiji)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, yomiji)).isEqualTo(4);
    }

    @Test
    @DisplayName("Also buffs a legendary creature already on the battlefield")
    void buffsLegendaryCreatureAlreadyOnBattlefield() {
        Permanent yomiji = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());

        assertThat(gqs.getEffectivePower(gd, yomiji)).isEqualTo(4);

        harness.addToBattlefield(player1, new DayOfDestiny());

        assertThat(gqs.getEffectivePower(gd, yomiji)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, yomiji)).isEqualTo(6);
    }

    @Test
    @DisplayName("An animated Day of Destiny buffs itself")
    void animatedDayOfDestinyAlsoGetsBonus() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent dayOfDestiny = harness.addToBattlefieldAndReturn(player1, new DayOfDestiny());

        assertThat(gqs.isCreature(gd, dayOfDestiny)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dayOfDestiny)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dayOfDestiny)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bonus is removed when Day of Destiny leaves the battlefield")
    void bonusRemovedWhenItLeaves() {
        Permanent dayOfDestiny = harness.addToBattlefieldAndReturn(player1, new DayOfDestiny());
        Permanent yomiji = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());

        assertThat(gqs.getEffectivePower(gd, yomiji)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(dayOfDestiny);

        assertThat(gqs.getEffectivePower(gd, yomiji)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, yomiji)).isEqualTo(4);
    }

    @Test
    @DisplayName("The bonus starts on resolution, not while Day of Destiny is on the stack")
    void bonusStartsWhenSpellResolves() {
        Permanent yomiji = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());
        Permanent opposingYomiji = harness.addToBattlefieldAndReturn(player2, new YomijiWhoBarsTheWay());
        Permanent gnarledMass = harness.addToBattlefieldAndReturn(player1, new GnarledMass());

        harness.castFromHand(player1, new DayOfDestiny(), "{3}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, yomiji)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, yomiji)).isEqualTo(4);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Day of Destiny");
        assertThat(gqs.getEffectivePower(gd, yomiji)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, yomiji)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingYomiji)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingYomiji)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, gnarledMass)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gnarledMass)).isEqualTo(3);
    }
}
