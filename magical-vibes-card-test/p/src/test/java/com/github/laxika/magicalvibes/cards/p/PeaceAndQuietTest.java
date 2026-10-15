package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuraFlux;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.n.NoMercy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeaceAndQuiet.class, AuraFlux.class, NoMercy.class, GiantCockroach.class})
class PeaceAndQuietTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast with only one enchantment target")
    void cannotCastWithOnlyOneTarget() {
        harness.addToBattlefield(player2, new AuraFlux());
        harness.setHand(player1, List.of(new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Aura Flux");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Peace and Quiet");
        harness.assertOnBattlefield(player2, "Aura Flux");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Destroys the remaining legal target when either target leaves the battlefield")
    void destroysRemainingLegalTarget(boolean removeFirstTarget) {
        harness.addToBattlefield(player2, new AuraFlux());
        harness.addToBattlefield(player2, new NoMercy());
        harness.addToBattlefield(player1, new AuraFlux());
        harness.setHand(player1, List.of(new PeaceAndQuiet(), new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID firstTarget = harness.getPermanentId(player2, "Aura Flux");
        UUID secondTarget = harness.getPermanentId(player2, "No Mercy");
        UUID extraTarget = harness.getPermanentId(player1, "Aura Flux");
        harness.castInstant(player1, 0, List.of(firstTarget, secondTarget));
        harness.castAndResolveInstant(player1, 0,
                List.of(removeFirstTarget ? firstTarget : secondTarget, extraTarget));

        harness.assertOnBattlefield(player2, removeFirstTarget ? "No Mercy" : "Aura Flux");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aura Flux");
        harness.assertInGraveyard(player2, "No Mercy");
        harness.assertNotOnBattlefield(player2, "Aura Flux");
        harness.assertNotOnBattlefield(player2, "No Mercy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not resolve when both targets have left the battlefield")
    void doesNotResolveWhenBothTargetsLeave() {
        harness.addToBattlefield(player2, new AuraFlux());
        harness.addToBattlefield(player2, new NoMercy());
        harness.setHand(player1, List.of(new PeaceAndQuiet(), new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        List<UUID> targets = List.of(harness.getPermanentId(player2, "Aura Flux"),
                harness.getPermanentId(player2, "No Mercy"));
        harness.castInstant(player1, 0, targets);
        harness.castAndResolveInstant(player1, 0, targets);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aura Flux");
        harness.assertInGraveyard(player2, "No Mercy");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Destroys two target enchantments")
    void destroysTwoTargetEnchantments() {
        harness.addToBattlefield(player2, new AuraFlux());
        harness.addToBattlefield(player2, new NoMercy());
        harness.setHand(player1, List.of(new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID firstTarget = harness.getPermanentId(player2, "Aura Flux");
        UUID secondTarget = harness.getPermanentId(player2, "No Mercy");
        harness.castAndResolveInstant(player1, 0, List.of(firstTarget, secondTarget));

        harness.assertNotOnBattlefield(player2, "Aura Flux");
        harness.assertNotOnBattlefield(player2, "No Mercy");
        harness.assertInGraveyard(player2, "Aura Flux");
        harness.assertInGraveyard(player2, "No Mercy");
    }

    @Test
    @DisplayName("Can destroy enchantments controlled by either player")
    void canDestroyEnchantmentsControlledByEitherPlayer() {
        harness.addToBattlefield(player1, new AuraFlux());
        harness.addToBattlefield(player2, new NoMercy());
        harness.setHand(player1, List.of(new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID ownTarget = harness.getPermanentId(player1, "Aura Flux");
        UUID opposingTarget = harness.getPermanentId(player2, "No Mercy");
        harness.castAndResolveInstant(player1, 0, List.of(ownTarget, opposingTarget));

        harness.assertInGraveyard(player1, "Aura Flux");
        harness.assertInGraveyard(player2, "No Mercy");
    }

    @Test
    @DisplayName("Requires two different enchantment targets")
    void requiresTwoDifferentTargets() {
        harness.addToBattlefield(player2, new AuraFlux());
        harness.setHand(player1, List.of(new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Aura Flux");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(targetId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        harness.addToBattlefield(player2, new AuraFlux());
        harness.addToBattlefield(player2, new GiantCockroach());
        harness.setHand(player1, List.of(new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID enchantmentId = harness.getPermanentId(player2, "Aura Flux");
        UUID creatureId = harness.getPermanentId(player2, "Giant Cockroach");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(enchantmentId, creatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }
}
