package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoryVeteran.class, LeoninScimitar.class})
class ArmoryVeteranTest extends BaseCardTest {

    @Test
    void withoutEquipmentDoesNotHaveMenace() {
        Permanent veteran = addVeteranReady(player1);

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isFalse();
    }

    @Test
    void whileEquippedHasMenace() {
        Permanent veteran = addVeteranReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(veteran.getId());

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isTrue();
    }

    @Test
    void losesMenaceWhenEquipmentIsDetached() {
        Permanent veteran = addVeteranReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(veteran.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isFalse();
    }

    private Permanent addVeteranReady(Player player) {
        Permanent permanent = new Permanent(new ArmoryVeteran());
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
