package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.v.VeteranDungeoneer;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HamaPasharRuinSeeker.class, VeteranDungeoneer.class, SoulWarden.class, GrizzlyBears.class})
class HamaPasharRuinSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Causes the same dungeon room ability to trigger twice")
    void doublesDungeonRoomAbility() {
        harness.addToBattlefield(player1, new HamaPasharRuinSeeker());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));

        harness.castFromHand(player1, new VeteranDungeoneer(), "{3}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
    }

    @Test
    @DisplayName("Does not double unrelated triggered abilities")
    void doesNotDoubleUnrelatedTriggers() {
        harness.addToBattlefield(player1, new HamaPasharRuinSeeker());
        harness.addToBattlefield(player1, new SoulWarden());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }
}
