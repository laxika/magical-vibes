package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Squawkroaster.class, GrizzlyBears.class, AirElemental.class, WallOfAir.class})
class SquawkroasterTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of distinct colors among controlled permanents")
    void powerEqualsDistinctControlledColors() {
        Permanent squawkroaster = addCreatureReady(player1, new Squawkroaster());

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squawkroaster)).isEqualTo(4);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new WallOfAir());

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squawkroaster)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts each color once and ignores permanents controlled by an opponent")
    void countsDistinctColorsOnlyFromController() {
        Permanent squawkroaster = addCreatureReady(player1, new Squawkroaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new WallOfAir());

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates when controlled permanents change")
    void powerUpdatesWithControlledPermanents() {
        Permanent squawkroaster = addCreatureReady(player1, new Squawkroaster());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(bears);

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(1);
    }

    @Test
    @DisplayName("A color present only on an opponent's battlefield does not increase power")
    void ignoresUniqueOpponentColor() {
        Permanent squawkroaster = addCreatureReady(player1, new Squawkroaster());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, squawkroaster)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unblocked Squawkroaster deals damage in both combat damage steps")
    void dealsDoubleStrikeCombatDamage() {
        addCreatureReady(player1, new Squawkroaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Power is defined in hand without counting Squawkroaster's own color")
    void powerInHandCountsOnlyBattlefieldColors() {
        Squawkroaster squawkroaster = new Squawkroaster();
        harness.setHand(player1, List.of(squawkroaster));

        assertThat(gqs.getEffectiveCardPower(gd, squawkroaster)).isZero();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.getEffectiveCardPower(gd, squawkroaster)).isEqualTo(2);
    }
}
