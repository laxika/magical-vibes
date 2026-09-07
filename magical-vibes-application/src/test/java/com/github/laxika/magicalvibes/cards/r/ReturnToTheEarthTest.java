package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReturnToTheEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Glorious Anthem"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Destroys target creature with flying")
    void destroysFlyingCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Air Elemental"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }
}
