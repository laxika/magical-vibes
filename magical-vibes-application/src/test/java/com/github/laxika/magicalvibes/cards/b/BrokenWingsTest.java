package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrokenWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new LiquimetalCoating());
        Permanent target = findPermanent(player2, "Liquimetal Coating");

        castBrokenWings(target);

        harness.assertNotOnBattlefield(player2, "Liquimetal Coating");
        harness.assertInGraveyard(player2, "Liquimetal Coating");
    }

    @Test
    @DisplayName("Destroys a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new IntangibleVirtue());
        Permanent target = findPermanent(player2, "Intangible Virtue");

        castBrokenWings(target);

        harness.assertNotOnBattlefield(player2, "Intangible Virtue");
        harness.assertInGraveyard(player2, "Intangible Virtue");
    }

    @Test
    @DisplayName("Destroys a target creature with flying")
    void destroysFlyingCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        Permanent target = findPermanent(player2, "Air Elemental");

        castBrokenWings(target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new BrokenWings()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or creature with flying");
    }

    private void castBrokenWings(Permanent target) {
        harness.setHand(player1, List.of(new BrokenWings()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
