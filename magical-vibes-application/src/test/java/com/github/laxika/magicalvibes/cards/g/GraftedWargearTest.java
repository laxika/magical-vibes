package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraftedWargearTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1);
        Permanent wargear = addWargearReady(player1);
        wargear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void equipZeroAttachesWithoutMana() {
        Permanent wargear = addWargearReady(player1);
        Permanent creature = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wargear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reEquippingSacrificesPreviouslyEquippedCreature() {
        Permanent wargear = addWargearReady(player1);
        Permanent creature1 = addCreatureReady(player1);
        Permanent creature2 = addCreatureReady(player1);
        wargear.setAttachedTo(creature1.getId());

        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(wargear.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature1.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addWargearReady(Player player) {
        Permanent wargear = new Permanent(new GraftedWargear());
        wargear.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wargear);
        return wargear;
    }
}
