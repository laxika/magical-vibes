package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaloHopper.class, GrizzlyBears.class})
class HaloHopperTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to pay Halo Hopper's generic mana cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HaloHopper()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId(), thirdCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Halo Hopper")).isEqualTo(1);
    }

    @Test
    void castsWithManaWithoutConvoking() {
        harness.setHand(player1, List.of(new HaloHopper()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Halo Hopper");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void colorlessSummoningSickCreatureCanConvokeAlongsideMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HaloHopper());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new HaloHopper()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Halo Hopper")).isEqualTo(2);
    }
}
