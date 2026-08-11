package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallopTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a blue creature with flying")
    void destroysBlueCreatureWithFlying() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new Wallop()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Air Elemental"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Destroys a black creature with flying")
    void destroysBlackCreatureWithFlying() {
        harness.addToBattlefield(player2, new SengirVampire());
        harness.setHand(player1, List.of(new Wallop()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Sengir Vampire"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sengir Vampire");
        harness.assertInGraveyard(player2, "Sengir Vampire");
    }

    @Test
    @DisplayName("Cannot target a flying creature that is neither blue nor black")
    void cannotTargetOtherColorFlyingCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        Permanent serraAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Wallop()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, serraAngel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue or black creature with flying");
    }
}
