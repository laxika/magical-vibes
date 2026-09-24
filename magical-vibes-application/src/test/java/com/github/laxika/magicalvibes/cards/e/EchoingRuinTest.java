package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AetherVial;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoingRuin.class, AetherVial.class, MyrMoonvessel.class, CrazedGoblin.class, DarksteelIngot.class})
class EchoingRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and every other artifact with the same name")
    void destroysTargetAndAllWithSameName() {
        harness.addToBattlefield(player2, new AetherVial());
        harness.addToBattlefield(player2, new AetherVial());
        harness.addToBattlefield(player1, new AetherVial());

        UUID targetId = harness.getPermanentId(player2, "Aether Vial");
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Aether Vial");
        harness.assertNotOnBattlefield(player2, "Aether Vial");
    }

    @Test
    @DisplayName("Leaves artifacts with different names on the battlefield")
    void leavesDifferentNames() {
        harness.addToBattlefield(player2, new AetherVial());
        harness.addToBattlefield(player2, new MyrMoonvessel());

        UUID targetId = harness.getPermanentId(player2, "Aether Vial");
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Aether Vial");
        harness.assertOnBattlefield(player2, "Myr Moonvessel");
    }

    @Test
    @DisplayName("Affects only same-name artifacts on the battlefield")
    void affectsOnlyBattlefieldPermanents() {
        harness.addToBattlefield(player2, new AetherVial());
        harness.setHand(player2, List.of(new AetherVial()));
        harness.setGraveyard(player2, List.of(new AetherVial()));

        UUID targetId = harness.getPermanentId(player2, "Aether Vial");
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Aether Vial");
        harness.assertInHand(player2, "Aether Vial");
        harness.assertInGraveyard(player2, "Aether Vial");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new CrazedGoblin());
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Crazed Goblin");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Does not destroy an indestructible artifact")
    void doesNotDestroyIndestructibleArtifact() {
        harness.addToBattlefield(player2, new DarksteelIngot());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Ingot");
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Ingot");
    }
}
