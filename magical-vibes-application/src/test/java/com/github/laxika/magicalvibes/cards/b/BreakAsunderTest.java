package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakAsunder.class, GloriousAnthem.class, GrizzlyBears.class, RodOfRuin.class})
class BreakAsunderTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new RodOfRuin());

        castBreakAsunder(harness.getPermanentId(player2, "Rod of Ruin"));

        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Destroys a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());

        castBreakAsunder(harness.getPermanentId(player2, "Glorious Anthem"));

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Rejects a creature target")
    void rejectsCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakAsunder()));
        addBreakAsunderMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1,
                0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Cycling discards Break Asunder and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BreakAsunder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Break Asunder");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castBreakAsunder(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BreakAsunder()));
        addBreakAsunderMana();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addBreakAsunderMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
