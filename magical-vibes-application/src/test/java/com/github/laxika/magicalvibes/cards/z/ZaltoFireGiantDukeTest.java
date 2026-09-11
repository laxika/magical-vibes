package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZaltoFireGiantDuke.class, ProdigalSorcerer.class})
class ZaltoFireGiantDukeTest extends BaseCardTest {

    @Test
    @DisplayName("When Zalto is dealt damage, its controller ventures into the dungeon")
    void dealtDamageVenturesIntoDungeon() {
        Permanent zalto = harness.addToBattlefieldAndReturn(player1, new ZaltoFireGiantDuke());
        Permanent pinger = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        pinger.setSummoningSick(false);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pinger), null,
                zalto.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }
}
