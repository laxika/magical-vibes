package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyeYoungAvenger.class, GrizzlyBears.class, Shock.class})
class HawkeyeYoungAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Adds Hawkeye's power to noncombat damage dealt to an opponent")
    void addsPowerToNoncombatDamageToOpponent() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeYoungAvenger());
        hawkeye.setPowerModifier(1);
        harness.setLife(player2, 20);
        castShock(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Adds Hawkeye's power to noncombat damage dealt to an opponent's permanent")
    void addsPowerToNoncombatDamageToOpponentPermanent() {
        harness.addToBattlefield(player1, new HawkeyeYoungAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castShock(bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not add Hawkeye's power to combat damage")
    void doesNotAddPowerToCombatDamage() {
        addCreatureReady(player1, new HawkeyeYoungAvenger());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void castShock(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
