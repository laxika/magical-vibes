package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.Justice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveOfReckoning.class, WallOfSwords.class, GiantSpider.class, HillGiant.class, Justice.class})
class WaveOfReckoningTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature deals damage to itself equal to its power")
    void eachCreatureDealsItsPowerToItself() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfSwords());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Wall of Swords");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Each creature is the source of its own damage")
    void eachCreatureIsItsOwnDamageSource() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Lethal self-damage puts the creature into its owner's graveyard")
    void lethalSelfDamageDestroysCreature() {
        harness.addToBattlefield(player1, new HillGiant());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }
}
