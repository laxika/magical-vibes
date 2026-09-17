package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeriasOutrider.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class MeriasOutriderTest extends BaseCardTest {

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
