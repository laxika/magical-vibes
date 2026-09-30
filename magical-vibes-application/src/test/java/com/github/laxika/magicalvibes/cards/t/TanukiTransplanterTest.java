package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TanukiTransplanter.class, GrizzlyBears.class, HillGiant.class})
class TanukiTransplanterTest extends BaseCardTest {

    @Test
    void attackingUnconfiguredTransplanterAddsManaEqualToItsPower() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        transplanter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void attackingEquippedCreatureAddsManaEqualToItsPower() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent creature = addCreatureReady(player1, new HillGiant());
        transplanter.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void reconfigureAttachesAndUnattachesTheTransplanter() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(transplanter.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, transplanter)).isFalse();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(transplanter.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, transplanter)).isTrue();
    }
}
