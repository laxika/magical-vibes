package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirshipCrash.class, AirElemental.class, GloriousAnthem.class, GrizzlyBears.class, Millstone.class})
class AirshipCrashTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Millstone()).getId();

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Destroys target creature with flying")
    void destroysFlyingCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AirElemental()).getId();

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        assertThatThrownBy(() -> castAndResolve(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new AirshipCrash()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Airship Crash");
        harness.assertNotInHand(player1, "Airship Crash");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Airship Crash");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the caster")
    void destroysOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Millstone()).getId();

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertInGraveyard(player1, "Millstone");
    }

    @Test
    @DisplayName("Cannot cycle without paying the full two mana")
    void cannotCycleWithInsufficientMana() {
        harness.setHand(player1, List.of(new AirshipCrash()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Airship Crash");
        harness.assertNotInGraveyard(player1, "Airship Crash");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve(UUID targetId) {
        harness.setHand(player1, List.of(new AirshipCrash()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
