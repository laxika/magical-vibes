package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Heliophial.class, DrossCrocodile.class})
class HeliophialTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Heliophial deals damage equal to its charge counters to a player")
    void sacrificeDealsDamageToPlayer() {
        Permanent heliophial = addReadyHeliophial(player1);
        heliophial.setCounterCount(CounterType.CHARGE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Heliophial");
    }

    @Test
    @DisplayName("Sacrificing Heliophial deals damage to a creature")
    void sacrificeDealsDamageToCreature() {
        Permanent heliophial = addReadyHeliophial(player1);
        heliophial.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent crocodile = addCreatureReady(player2, new DrossCrocodile());

        harness.activateAbility(player1, 0, null, crocodile.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dross Crocodile");
        harness.assertInGraveyard(player1, "Heliophial");
    }

    @Test
    @DisplayName("Sacrificing Heliophial with no charge counters deals no damage")
    void sacrificeWithNoCountersDealsNoDamage() {
        addReadyHeliophial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Heliophial");
    }

    @Test
    @DisplayName("Heliophial can activate while tapped because its ability has no tap cost")
    void abilityDoesNotRequireTapping() {
        Permanent heliophial = addReadyHeliophial(player1);
        heliophial.tap();
        heliophial.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sunburst counts distinct colors of mana spent to cast Heliophial")
    void sunburstCountsDistinctColorsSpent() {
        harness.setHand(player1, List.of(new Heliophial()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent heliophial = findPermanent(player1, "Heliophial");
        assertThat(heliophial.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifice is paid even when Heliophial's target becomes illegal")
    void sacrificeIsPaidWhenTargetBecomesIllegal() {
        Permanent heliophial = addReadyHeliophial(player1);
        heliophial.setCounterCount(CounterType.CHARGE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent crocodile = addCreatureReady(player2, new DrossCrocodile());

        harness.activateAbility(player1, 0, null, crocodile.getId());
        gd.playerBattlefields.get(player2.getId()).remove(crocodile);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heliophial");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Heliophial can activate immediately because it is not a creature")
    void canActivateWithoutWaitingWhenItEnters() {
        Permanent heliophial = harness.addToBattlefieldAndReturn(player1, new Heliophial());
        heliophial.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private Permanent addReadyHeliophial(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Heliophial());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
