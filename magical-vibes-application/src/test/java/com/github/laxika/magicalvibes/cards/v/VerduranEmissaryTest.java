package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerduranEmissaryTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotDestroy() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Verduran Emissary");
        harness.assertOnBattlefield(player2, "Rod of Ruin");
    }

    @Test
    void kickedDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addKickedMana();
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    void kickedDestroyCannotBeRegenerated() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addKickedMana();
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Rod of Ruin");
        target.setRegenerationShield(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    void cannotKickTargetNonArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VerduranEmissary()));
        addKickedMana();
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
