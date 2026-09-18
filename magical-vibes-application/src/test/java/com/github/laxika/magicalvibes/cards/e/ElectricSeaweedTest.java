package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElectricSeaweed.class, BalduvianBears.class, FyndhornElves.class, Shock.class, WallOfWood.class})
class ElectricSeaweedTest extends BaseCardTest {

    @Test
    @DisplayName("Its temporary death trigger damages non-Wall creatures but not Walls")
    void deathTriggerDamagesNonWallCreatures() {
        Permanent nonWall = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new FyndhornElves());
        Permanent seaweed = castElectricSeaweed(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, dyingCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(nonWall.getMarkedDamage()).isEqualTo(1);
        assertThat(wall.getMarkedDamage()).isZero();
        assertThat(seaweed.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to any target")
    void tapAbilityDealsDamageToTargetPlayer() {
        Permanent seaweed = addReadySeaweed(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(seaweed.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent castElectricSeaweed(Player player) {
        harness.setHand(player, List.of(new ElectricSeaweed()));
        harness.addMana(player, ManaColor.RED, 4);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player, "Electric Seaweed");
    }

    private Permanent addReadySeaweed(Player player) {
        Permanent seaweed = harness.addToBattlefieldAndReturn(player, new ElectricSeaweed());
        seaweed.setSummoningSick(false);
        return seaweed;
    }
}
