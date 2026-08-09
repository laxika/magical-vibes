package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConstantMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage when it resolves")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new ConstantMists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        harness.assertInGraveyard(player1, "Constant Mists");
    }

    @Test
    @DisplayName("Paying buyback sacrifices a land and returns Constant Mists to hand")
    void buybackSacrificesLandAndReturnsToHand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new ConstantMists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrificeAndBuyback(player1, 0, null, harness.getPermanentId(player1, "Forest"));
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();
        assertThat(findPermanents(player1, "Forest")).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Constant Mists");
        harness.assertNotInGraveyard(player1, "Constant Mists");
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Buyback cannot sacrifice a nonland permanent")
    void buybackRequiresLand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConstantMists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrificeAndBuyback(
                player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Constant Mists");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
