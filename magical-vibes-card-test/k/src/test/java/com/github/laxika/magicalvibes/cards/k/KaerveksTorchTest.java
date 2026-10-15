package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Dissipate;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaerveksTorch.class, Dissipate.class, FeralShadow.class})
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
        harness.addToBattlefield(player2, new FeralShadow());
        harness.setHand(player1, List.of(new KaerveksTorch()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, harness.getPermanentId(player2, "Feral Shadow"));

        harness.assertNotOnBattlefield(player2, "Feral Shadow");
        harness.assertInGraveyard(player2, "Feral Shadow");
    }

    @Test
    @DisplayName("Spells targeting it on the stack cost {2} more to cast")
    void taxesSpellsTargetingIt() {
        KaerveksTorch torch = new KaerveksTorch();
        harness.setHand(player1, List.of(torch));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 4);

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

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, torch.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(torch);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Taxes spells cast by its controller that target it")
    void taxesSpellsCastByItsController() {
        KaerveksTorch torch = new KaerveksTorch();
        harness.setHand(player1, List.of(torch));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, player2.getId());

        harness.setHand(player1, List.of(new Dissipate()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, torch.getId()))
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Deals no damage when X is zero")
    void dealsNoDamageWhenXIsZero() {
        harness.setHand(player1, List.of(new KaerveksTorch()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Kaervek's Torch");
    }

    @Test
    @DisplayName("Can deal damage to its controller")
    void dealsDamageToItsController() {
        harness.setHand(player1, List.of(new KaerveksTorch()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not tax spells targeting another spell while Torch is on the stack")
    void doesNotTaxSpellsTargetingOtherSpells() {
        KaerveksTorch torch = new KaerveksTorch();
        Dissipate opposingCounter = new Dissipate();
        harness.setHand(player1, List.of(torch, new Dissipate()));
        harness.setHand(player2, List.of(opposingCounter));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, torch.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, opposingCounter.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingCounter);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Kaervek's Torch");
    }
}
