package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boommobile;
import com.github.laxika.magicalvibes.cards.b.BrightfieldMustang;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoadRageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals two damage with no Mounts or Vehicles")
    void dealsBaseDamage() {
        harness.addToBattlefield(player2, new MahamotiDjinn());

        castRoadRage(harness.getPermanentId(player2, "Mahamoti Djinn"));

        assertThat(findPermanent(player2, "Mahamoti Djinn").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds one damage for each Mount or Vehicle you control")
    void countsMountsAndVehiclesYouControl() {
        harness.addToBattlefield(player1, new BrightfieldMustang());
        harness.addToBattlefield(player1, new Boommobile());
        harness.addToBattlefield(player2, new BrightfieldMustang());
        harness.addToBattlefield(player2, new MahamotiDjinn());

        castRoadRage(harness.getPermanentId(player2, "Mahamoti Djinn"));

        assertThat(findPermanent(player2, "Mahamoti Djinn").getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can deal damage to a planeswalker")
    void damagesPlaneswalker() {
        harness.addToBattlefield(player1, new BrightfieldMustang());
        Permanent planeswalker = new Permanent(new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        castRoadRage(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new RoadRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRoadRage(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RoadRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
