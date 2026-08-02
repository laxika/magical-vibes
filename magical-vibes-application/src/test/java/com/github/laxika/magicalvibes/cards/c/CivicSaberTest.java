package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CivicSaberTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Civic Saber to a creature you control")
    void equipAttachesToCreature() {
        Permanent saber = addCivicSaberReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(saber.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0 for each color")
    void boostScalesWithColors() {
        Permanent creature = addCreatureReady(player1, new QasaliAmbusher());
        Permanent saber = addCivicSaberReady(player1);
        saber.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Civic Saber gives no bonus to a colorless creature")
    void colorlessCreatureGetsNoBonus() {
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        Permanent saber = addCivicSaberReady(player1);
        saber.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private Permanent addCivicSaberReady(Player player) {
        Permanent permanent = new Permanent(new CivicSaber());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
