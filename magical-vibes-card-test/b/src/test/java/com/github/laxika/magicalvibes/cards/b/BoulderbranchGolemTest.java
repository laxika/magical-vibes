package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MachineOverMatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoulderbranchGolem.class, GiantGrowth.class, MachineOverMatter.class})
class BoulderbranchGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains life equal to its normal power")
    void normalCastGainsLifeEqualToPower() {
        prepareMainPhase();

        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new BoulderbranchGolem(), "{7}");
        resolveCreatureAndEnterTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Prototype entering the battlefield gains life equal to its prototype power")
    void prototypeCastGainsLifeEqualToPrototypePower() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new BoulderbranchGolem()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveCreatureAndEnterTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void usesPowerWhenTriggerResolves() {
        prepareMainPhase();
        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new BoulderbranchGolem(), "{7}");
        harness.passBothPriorities();

        var golem = findPermanent(player1, "Boulderbranch Golem");
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, golem.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 9);
    }

    @Test
    void usesLastKnownPrototypePowerAfterReturningToHand() {
        prepareMainPhase();
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new BoulderbranchGolem()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        var golem = findPermanent(player1, "Boulderbranch Golem");
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, golem.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void resolveCreatureAndEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
