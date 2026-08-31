package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DeepWater;
import com.github.laxika.magicalvibes.cards.g.GhostShip;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Riptide.class, GhostShip.class, Squire.class, DeepWater.class})
class RiptideTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all blue creatures and leaves nonblue creatures untapped")
    void tapsAllBlueCreatures() {
        Permanent ownBlueCreature = addCreatureReady(player1, new GhostShip());
        Permanent opposingBlueCreature = addCreatureReady(player2, new GhostShip());
        Permanent ownNonblueCreature = addCreatureReady(player1, new Squire());
        Permanent opposingNonblueCreature = addCreatureReady(player2, new Squire());

        harness.castFromHand(player1, new Riptide(), "{U}");
        harness.passBothPriorities();

        assertThat(ownBlueCreature.isTapped()).isTrue();
        assertThat(opposingBlueCreature.isTapped()).isTrue();
        assertThat(ownNonblueCreature.isTapped()).isFalse();
        assertThat(opposingNonblueCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap a blue noncreature permanent")
    void doesNotTapBlueNoncreaturePermanent() {
        Permanent blueNoncreature = harness.addToBattlefieldAndReturn(player1, new DeepWater());

        harness.castFromHand(player1, new Riptide(), "{U}");
        harness.passBothPriorities();

        assertThat(blueNoncreature.isTapped()).isFalse();
    }
}
