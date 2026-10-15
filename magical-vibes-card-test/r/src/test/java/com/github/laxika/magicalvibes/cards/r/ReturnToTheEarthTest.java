package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.u.UginsConstruct;
import com.github.laxika.magicalvibes.cards.v.ValorousStance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnToTheEarth.class, AirElemental.class, GloriousAnthem.class, GrizzlyBears.class,
        LeoninScimitar.class, UginsConstruct.class, ValorousStance.class, RealityShift.class})
class ReturnToTheEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Leonin Scimitar"));

        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Glorious Anthem"));

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Destroys target creature with flying")
    void destroysFlyingCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Air Elemental"));

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

    @Test
    @DisplayName("Destroys an artifact creature without flying")
    void destroysNonFlyingArtifactCreature() {
        harness.addToBattlefield(player2, new UginsConstruct());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Ugin's Construct"));

        harness.assertNotOnBattlefield(player2, "Ugin's Construct");
        harness.assertInGraveyard(player2, "Ugin's Construct");
    }

    @Test
    @DisplayName("Can destroy a permanent controlled by its caster")
    void destroysOwnPermanent() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Leonin Scimitar"));

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Does not destroy a flying creature that gains indestructible in response")
    void respectsIndestructibleGrantedInResponse() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.setHand(player2, List.of(new ValorousStance()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.WHITE, 2);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Return to the Earth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not destroy another creature when its target leaves the battlefield")
    void doesNotRetargetWhenTargetLeaves() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new UginsConstruct());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.setHand(player2, List.of(new RealityShift()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Air Elemental"));
        harness.assertOnBattlefield(player2, "Ugin's Construct");
        harness.assertInGraveyard(player1, "Return to the Earth");
        assertThat(gd.stack).isEmpty();
    }
}
