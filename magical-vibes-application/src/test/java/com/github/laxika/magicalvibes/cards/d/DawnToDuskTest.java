package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DawnToDuskTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 returns an enchantment card from the graveyard to hand")
    void returnsEnchantmentFromGraveyard() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0, pacifism.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Mode 0 cannot target a non-enchantment card")
    void mode0ExcludesNonEnchantments() {
        Pacifism pacifism = new Pacifism();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(pacifism, bears));
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 1 destroys a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 1, harness.getPermanentId(player2, "Glorious Anthem"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Mode 1 cannot target a creature")
    void cannotDestroyCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 returns an enchantment and destroys an enchantment")
    void returnsAndDestroysEnchantment() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 2, pacifism.getId(), null,
                List.of(pacifism.getId(), harness.getPermanentId(player2, "Glorious Anthem")), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }
}
