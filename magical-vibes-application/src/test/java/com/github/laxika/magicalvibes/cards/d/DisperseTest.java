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
        harness.addToBattlefield(player2, new StonybrookBanneret());
        UUID targetId = harness.getPermanentId(player2, "Stonybrook Banneret");
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Stonybrook Banneret");
        harness.assertInHand(player2, "Stonybrook Banneret");
    }

    @Test
    @DisplayName("Resolving bounces target artifact to owner's hand")
    void bouncesArtifact() {
        harness.addToBattlefield(player2, new ThornbiteStaff());
        UUID targetId = harness.getPermanentId(player2, "Thornbite Staff");
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Thornbite Staff");
        harness.assertInHand(player2, "Thornbite Staff");
    }

    @Test
    @DisplayName("Resolving bounces target enchantment to owner's hand")
    void bouncesEnchantment() {
        harness.addToBattlefield(player2, new Bitterblossom());
        UUID targetId = harness.getPermanentId(player2, "Bitterblossom");
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
        harness.addToBattlefield(player2, new Mutavault());
        UUID targetId = harness.getPermanentId(player2, "Mutavault");
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
        harness.addToBattlefield(player1, new ThornbiteStaff());
        UUID targetId = harness.getPermanentId(player1, "Thornbite Staff");
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
        harness.addToBattlefield(player2, new StonybrookBanneret());
        UUID targetId = harness.getPermanentId(player2, "Stonybrook Banneret");
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Disperse");
    }
}
