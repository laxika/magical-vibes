package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollateralDamageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 3 damage to a target player")
    void sacrificesCreatureAndDealsDamageToPlayer() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        harness.setLife(player2, 20);

        prepareCard();
        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals 3 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        prepareCard();
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        prepareCard();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new CollateralDamage()));
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
