package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumingCorruption.class, BenalishCavalry.class, NicolBolasPlaneswalker.class,
        Plains.class, Swamp.class})
class ConsumingCorruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to a creature and gains life equal to controlled Swamps")
    void dealsDamageToCreatureAndGainsLifeEqualToControlledSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new ConsumingCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 10);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        harness.assertNotOnBattlefield(player2, "Benalish Cavalry");
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void dealsDamageToPlaneswalkerAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ConsumingCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 10);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Counts Swamps at resolution and only under the spell controller's control")
    void countsSwampsAtResolutionUnderSpellControllersControl() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new ConsumingCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 10);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof Swamp);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new ConsumingCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }
}
