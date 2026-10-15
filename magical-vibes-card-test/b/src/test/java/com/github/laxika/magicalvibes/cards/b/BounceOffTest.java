package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.g.GasGuzzler;
import com.github.laxika.magicalvibes.cards.m.MonumentToEndurance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BounceOff.class, GasGuzzler.class, AirResponseUnit.class, MonumentToEndurance.class})
class BounceOffTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GasGuzzler());
        harness.setHand(player1, List.of(new BounceOff()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Gas Guzzler");
        harness.assertInHand(player2, "Gas Guzzler");
    }

    @Test
    void returnsTargetVehicleToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        harness.setHand(player1, List.of(new BounceOff()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Air Response Unit");
        harness.assertInHand(player2, "Air Response Unit");
    }

    @Test
    void cannotTargetNoncreatureNonVehiclePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MonumentToEndurance());
        harness.setHand(player1, List.of(new BounceOff()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    void canReturnYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GasGuzzler());
        harness.setHand(player1, List.of(new BounceOff()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Gas Guzzler");
        harness.assertInHand(player1, "Gas Guzzler");
        harness.assertInGraveyard(player1, "Bounce Off");
    }

    @Test
    void returnsStolenCreatureToOwnerInsteadOfController() {
        GasGuzzler creature = new GasGuzzler();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new BounceOff()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Gas Guzzler");
        harness.assertInHand(player2, "Gas Guzzler");
        harness.assertNotInHand(player1, "Gas Guzzler");
    }

    @Test
    void doesNotReturnAnotherCreatureWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GasGuzzler());
        harness.addToBattlefield(player2, new AirResponseUnit());
        harness.setHand(player1, List.of(new BounceOff()));
        harness.setHand(player2, List.of(new BounceOff()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Gas Guzzler");
        harness.assertOnBattlefield(player2, "Air Response Unit");
        harness.assertInGraveyard(player1, "Bounce Off");
        harness.assertInGraveyard(player2, "Bounce Off");
    }
}
