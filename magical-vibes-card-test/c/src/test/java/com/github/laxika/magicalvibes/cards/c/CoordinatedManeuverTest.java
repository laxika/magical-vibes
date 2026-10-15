package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoordinatedManeuver.class, ChandraNalaar.class, GloriousAnthem.class, GrizzlyBears.class})
class CoordinatedManeuverTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToCreaturesControlled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);

        cast(0, target);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1, target);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void eachModeRejectsAnIllegalTarget() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        assertThatThrownBy(() -> cast(0, enchantment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(1, creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }

    @Test
    void damagesCreatureWithoutCountingOpponentsCreaturesOrOtherPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(0, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void dealsNoDamageWithNoControlledCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, target);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoordinatedManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, 0, target.getId());

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canDestroyItsControllersEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        cast(1, target);

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new CoordinatedManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
