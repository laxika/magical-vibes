package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Firebreathing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyArmor;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BakisCurseTest extends BaseCardTest {

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player owner,
                                 com.github.laxika.magicalvibes.model.Card aura, Permanent creature) {
        Permanent auraPerm = new Permanent(aura);
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(owner.getId()).add(auraPerm);
        return auraPerm;
    }

    private void castCurse() {
        harness.setHand(player1, List.of(new BakisCurse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 2 damage per Aura attached to that creature")
    void dealsTwoDamagePerAura() {
        Permanent oneAura = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, new HolyStrength(), oneAura);

        Permanent twoAuras = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player2, new HolyStrength(), twoAuras);
        attachAura(player2, new HolyArmor(), twoAuras);

        castCurse();

        assertThat(oneAura.getMarkedDamage()).isEqualTo(2);
        assertThat(twoAuras.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Unenchanted creatures take no damage")
    void unenchantedCreatureTakesNoDamage() {
        Permanent bare = addCreatureReady(player2, new GrizzlyBears());

        castCurse();

        assertThat(bare.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bare.getId()));
    }

    @Test
    @DisplayName("A creature dies when the Aura-scaled damage is lethal")
    void lethalDamageDestroysCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player2, new Firebreathing(), bears);
        attachAura(player2, new Firebreathing(), bears);

        castCurse();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
    }
}
