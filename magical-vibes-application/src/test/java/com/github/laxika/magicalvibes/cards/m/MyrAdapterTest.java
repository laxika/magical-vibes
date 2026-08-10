package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyrAdapterTest extends BaseCardTest {

    @Test
    void withoutAttachedEquipmentIs1x1() {
        Permanent adapter = addAdapterReady(player1);

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(1);
    }

    @Test
    void eachAttachedEquipmentAddsOneToPowerAndToughness() {
        Permanent adapter = addAdapterReady(player1);
        Permanent scimitar1 = addScimitarReady(player1);
        Permanent scimitar2 = addScimitarReady(player1);

        scimitar1.setAttachedTo(adapter.getId());
        scimitar2.setAttachedTo(adapter.getId());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(5);
    }

    @Test
    void equipmentNotAttachedToAdapterDoesNotCount() {
        Permanent adapter = addAdapterReady(player1);
        Permanent otherCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(otherCreature);
        Permanent scimitar = addScimitarReady(player1);

        scimitar.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(1);
    }

    @Test
    void attachedEquipmentFromOpponentStillCounts() {
        Permanent adapter = addAdapterReady(player1);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(adapter.getId());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(3);
    }

    private Permanent addAdapterReady(Player player) {
        Permanent permanent = new Permanent(new MyrAdapter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addScimitarReady(Player player) {
        Permanent permanent = new Permanent(new LeoninScimitar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
