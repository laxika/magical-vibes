package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WoodedRidgeline;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeriasOutrider.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class,
        WoodedRidgeline.class})
class MeriasOutriderTest extends BaseCardTest {

    @Test
    void dealsFiveDamageWithAllBasicLandTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new MeriasOutrider()));
        harness.addMana(player1, ManaColor.RED, 5);
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, opponentLife - 5);
    }

    @Test
    void countsBothTypesOfANonbasicLandWithoutCountingDuplicates() {
        harness.addToBattlefield(player1, new WoodedRidgeline());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new MeriasOutrider()));
        harness.addMana(player1, ManaColor.RED, 5);
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, opponentLife - 2);
    }

    @Test
    void countsLandTypesWhenTheTriggerResolves() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new MeriasOutrider()));
        harness.addMana(player1, ManaColor.RED, 5);
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        harness.assertLife(player2, opponentLife - 2);
    }

    @Test
    void dealsDamageToEachOpponentEqualToDomain() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new MeriasOutrider()));
        harness.addMana(player1, ManaColor.RED, 5);
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 3);
    }

    @Test
    void countsEachBasicLandTypeOnlyOnceAndIgnoresOpponentsLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new MeriasOutrider()));
        harness.addMana(player1, ManaColor.RED, 5);
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 2);
    }

    @Test
    void dealsNoDamageWithNoBasicLandTypes() {
        harness.setHand(player1, List.of(new MeriasOutrider()));
        harness.addMana(player1, ManaColor.RED, 5);
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }
}
