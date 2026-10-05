package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagmaticForce.class, GrizzlyBears.class, KrakenHatchling.class})
class MagmaticForceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a chosen player at the beginning of each upkeep")
    void dealsDamageToChosenPlayerAtEachUpkeep() {
        harness.addToBattlefield(player1, new MagmaticForce());
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to a chosen creature at the beginning of upkeep")
    void dealsDamageToChosenCreature() {
        harness.addToBattlefield(player1, new MagmaticForce());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
    }

    @Test
    @DisplayName("Triggers again on the opponent's upkeep after its controller's upkeep")
    void triggersOnBothPlayersUpkeeps() {
        harness.addToBattlefield(player1, new MagmaticForce());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Can target itself and deals exactly 3 damage")
    void canDamageItself() {
        Permanent force = harness.addToBattlefieldAndReturn(player1, new MagmaticForce());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, force.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, force.getId())).isSameAs(force);
        assertThat(force.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can damage its controller's other creature without destroying it")
    void canDamageFriendlyCreature() {
        harness.addToBattlefield(player1, new MagmaticForce());
        Permanent hatchling = harness.addToBattlefieldAndReturn(player1, new KrakenHatchling());

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, hatchling.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, hatchling.getId())).isSameAs(hatchling);
        assertThat(hatchling.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The upkeep ability resolves even if Magmatic Force leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent force = harness.addToBattlefieldAndReturn(player1, new MagmaticForce());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, force));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInHand(player1, "Magmatic Force");
    }

    @Test
    @DisplayName("Does not retarget when its chosen creature leaves before resolution")
    void doesNotRetargetMissingCreature() {
        Permanent force = harness.addToBattlefieldAndReturn(player1, new MagmaticForce());
        Permanent hatchling = harness.addToBattlefieldAndReturn(player2, new KrakenHatchling());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, hatchling.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, hatchling));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Kraken Hatchling");
        assertThat(force.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
