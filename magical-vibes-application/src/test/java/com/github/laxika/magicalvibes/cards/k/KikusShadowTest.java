package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KikusShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Kiku's Shadow destroys a creature when its power is lethal")
    void destroysCreatureWhenPowerIsLethal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Kiku's Shadow marks damage equal to the target's power")
    void marksDamageEqualToPower() {
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Wall of Swords"));
        harness.passBothPriorities();

        Permanent wall = findPermanent(player2, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Kiku's Shadow cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature");

        harness.assertOnBattlefield(player2, "Plains");
    }
}
