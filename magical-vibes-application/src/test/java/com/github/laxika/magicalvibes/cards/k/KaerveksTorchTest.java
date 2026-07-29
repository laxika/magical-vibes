package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KaerveksTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to a player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new KaerveksTorch()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals X damage to a creature, destroying it")
    void dealsXDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KaerveksTorch()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spells targeting it on the stack cost {2} more to cast")
    void taxesSpellsTargetingIt() {
        KaerveksTorch torch = new KaerveksTorch();
        harness.setHand(player1, List.of(torch));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, torch.getId()))
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A spell targeting it resolves when the extra {2} is paid")
    void countersItWhenTaxPaid() {
        KaerveksTorch torch = new KaerveksTorch();
        harness.setHand(player1, List.of(torch));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, torch.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kaervek's Torch");
        harness.assertLife(player2, 20);
    }
}
