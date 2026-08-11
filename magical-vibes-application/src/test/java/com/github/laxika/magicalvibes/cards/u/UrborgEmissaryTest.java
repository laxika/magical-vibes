package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UrborgEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB does not return a permanent")
    void withoutKickerDoesNotReturnPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UrborgEmissary()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Urborg Emissary");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB returns the target permanent to its owner's hand")
    void kickedReturnsTargetPermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new UrborgEmissary()));
        addKickedMana();
        UUID landId = harness.getPermanentId(player2, "Forest");

        harness.castKickedCreature(player1, 0, landId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertOnBattlefield(player1, "Urborg Emissary");
    }

    @Test
    @DisplayName("When kicked, a creature is also a legal target")
    void kickedReturnsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UrborgEmissary()));
        addKickedMana();
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castKickedCreature(player1, 0, creatureId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
