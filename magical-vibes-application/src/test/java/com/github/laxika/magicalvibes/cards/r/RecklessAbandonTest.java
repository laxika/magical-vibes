package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecklessAbandonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 4 damage to a target player")
    void sacrificesCreatureAndDealsDamageToPlayer() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals 4 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        Permanent target = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
