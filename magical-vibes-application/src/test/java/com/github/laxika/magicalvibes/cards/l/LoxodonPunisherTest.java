package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoxodonPunisherTest extends BaseCardTest {

    @Test
    void withoutEquipmentIs2x2() {
        Permanent punisher = addPunisherReady(player1);

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(2);
    }

    @Test
    void eachAttachedEquipmentAddsTwoToPowerAndToughness() {
        Permanent punisher = addPunisherReady(player1);
        Permanent scimitar1 = addScimitarReady(player1);
        Permanent scimitar2 = addScimitarReady(player1);

        scimitar1.setAttachedTo(punisher.getId());
        scimitar2.setAttachedTo(punisher.getId());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(8);
    }

    @Test
    void equipmentNotAttachedToPunisherDoesNotCount() {
        Permanent punisher = addPunisherReady(player1);
        Permanent otherCreature = addPunisherReady(player1);
        Permanent scimitar = addScimitarReady(player1);

        scimitar.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(2);
    }

    @Test
    void attachedEquipmentFromOpponentStillCounts() {
        Permanent punisher = addPunisherReady(player1);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(punisher.getId());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(5);
    }

    private Permanent addPunisherReady(Player player) {
        Permanent perm = new Permanent(new LoxodonPunisher());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addScimitarReady(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
