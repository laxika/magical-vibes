package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.s.StonybrookBanneret;
import com.github.laxika.magicalvibes.cards.t.ThornbiteStaff;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disperse.class, Bitterblossom.class, Mutavault.class, StonybrookBanneret.class,
        ThornbiteStaff.class})
class DisperseTest extends BaseCardTest {

    // ===== Can target nonland permanents =====

    @Test
    @DisplayName("Resolving bounces target creature to owner's hand")
    void bouncesCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new StonybrookBanneret()).getId();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Stonybrook Banneret");
        harness.assertInHand(player2, "Stonybrook Banneret");
    }

    @Test
    @DisplayName("Resolving bounces target artifact to owner's hand")
    void bouncesArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ThornbiteStaff()).getId();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Thornbite Staff");
        harness.assertInHand(player2, "Thornbite Staff");
    }

    @Test
    @DisplayName("Resolving bounces target enchantment to owner's hand")
    void bouncesEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Bitterblossom()).getId();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Bitterblossom");
        harness.assertInHand(player2, "Bitterblossom");
    }

    // ===== Cannot target lands =====

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new StonybrookBanneret()); // valid target so spell is playable
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mutavault()).getId();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    // ===== Can bounce own permanents =====

    @Test
    @DisplayName("Can bounce own permanent")
    void canBounceOwnPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new ThornbiteStaff()).getId();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Thornbite Staff");
        harness.assertInHand(player1, "Thornbite Staff");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new StonybrookBanneret()).getId();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Disperse");
    }

    @Test
    @DisplayName("Returns a permanent to its owner rather than its controller")
    void returnsToOwnerInsteadOfController() {
        StonybrookBanneret creature = new StonybrookBanneret();
        creature.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, creature).getId();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Stonybrook Banneret");
        harness.assertInHand(player1, "Stonybrook Banneret");
        harness.assertNotInHand(player2, "Stonybrook Banneret");
    }

    @Test
    @DisplayName("Cannot target an animated land creature")
    void cannotTargetAnimatedLand() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mutavault()).getId();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
        harness.assertOnBattlefield(player2, "Mutavault");
    }

    @Test
    @DisplayName("A second Disperse can remove the target in response")
    void fizzlesAfterTargetIsBouncedInResponse() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new StonybrookBanneret()).getId();
        harness.setHand(player1, List.of(new Disperse()));
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Stonybrook Banneret");
        harness.assertInHand(player2, "Stonybrook Banneret");
        harness.assertInGraveyard(player1, "Disperse");
        harness.assertInGraveyard(player2, "Disperse");
        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
