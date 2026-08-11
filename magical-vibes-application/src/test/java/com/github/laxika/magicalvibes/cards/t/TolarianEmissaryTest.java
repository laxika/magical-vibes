package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TolarianEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB does not destroy an enchantment")
    void withoutKickerDoesNotDestroyEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Tolarian Emissary");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB destroys the target enchantment")
    void kickedDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addKickedMana();
        UUID enchantmentId = harness.getPermanentId(player2, "Glorious Anthem");

        harness.castKickedCreature(player1, 0, enchantmentId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("When kicked, only an enchantment is a legal target")
    void kickedOnlyTargetsEnchantments() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addKickedMana();
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
