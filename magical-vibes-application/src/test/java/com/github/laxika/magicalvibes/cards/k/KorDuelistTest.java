package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KorDuelistTest extends BaseCardTest {

    @Test
    void withoutEquipmentDoesNotHaveDoubleStrike() {
        Permanent duelist = addDuelistReady(player1);

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void whileEquippedHasDoubleStrike() {
        Permanent duelist = addDuelistReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(duelist.getId());

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void losesDoubleStrikeWhenEquipmentIsDetached() {
        Permanent duelist = addDuelistReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(duelist.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addDuelistReady(Player player) {
        Permanent permanent = new Permanent(new KorDuelist());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent permanent = new Permanent(new LeoninScimitar());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
