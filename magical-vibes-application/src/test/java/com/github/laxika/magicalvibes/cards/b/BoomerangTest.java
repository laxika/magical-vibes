package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdventurersGuildhouse;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.f.FieldOfDreams;
import com.github.laxika.magicalvibes.cards.g.GauntletsOfChaos;
import com.github.laxika.magicalvibes.cards.u.UnderworldDreams;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Boomerang.class, AdventurersGuildhouse.class, DurkwoodBoars.class, FieldOfDreams.class, GauntletsOfChaos.class, UnderworldDreams.class})
class BoomerangTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Boomerang puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Durkwood Boars");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving returns target creature to owner's hand")
    void resolvingReturnsCreatureToHand() {
        harness.addToBattlefield(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Durkwood Boars");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Durkwood Boars");
        harness.assertInHand(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Resolving returns target enchantment to owner's hand")
    void resolvingReturnsEnchantmentToHand() {
        harness.addToBattlefield(player2, new UnderworldDreams());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Underworld Dreams");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Underworld Dreams");
        harness.assertInHand(player2, "Underworld Dreams");
    }

    @Test
    @DisplayName("Resolving returns target enchantment to owner's hand")
    void resolvingReturnsEnchantmentToHandUpstreamReview() {
        harness.addToBattlefield(player2, new FieldOfDreams());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Field of Dreams");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Field of Dreams");
        harness.assertInHand(player2, "Field of Dreams");
    }

    @Test
    @DisplayName("Resolving returns target land to owner's hand")
    void resolvingReturnsLandToHand() {
        harness.addToBattlefield(player2, new AdventurersGuildhouse());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Adventurers' Guildhouse");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Adventurers' Guildhouse");
        harness.assertInHand(player2, "Adventurers' Guildhouse");
    }

    @Test
    @DisplayName("Can bounce own permanent")
    void canBounceOwnPermanent() {
        harness.addToBattlefield(player1, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player1, "Durkwood Boars");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Durkwood Boars");
        harness.assertInHand(player1, "Durkwood Boars");
    }

    @Test
    @DisplayName("Returns an opponent-controlled permanent to its owner's hand")
    void returnsPermanentToOwnerHandWhenControlledByOpponent() {
        DurkwoodBoars ownedBoars = new DurkwoodBoars();
        ownedBoars.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, ownedBoars);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Durkwood Boars");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInHand(player1, "Durkwood Boars");
        harness.assertNotInHand(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Resolving returns target artifact to owner's hand")
    void resolvingReturnsArtifactToHand() {
        harness.addToBattlefield(player2, new GauntletsOfChaos());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Gauntlets of Chaos");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Gauntlets of Chaos");
        harness.assertInHand(player2, "Gauntlets of Chaos");
    }

    @Test
    @DisplayName("Boomerang goes to graveyard after resolving")
    void boomerangGoesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Durkwood Boars");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Boomerang");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Durkwood Boars");
        // Boomerang still goes to graveyard
        harness.assertInGraveyard(player1, "Boomerang");
    }

    @Test
    @DisplayName("Target creature does not go to graveyard (it goes to hand)")
    void targetDoesNotGoToGraveyard() {
        harness.addToBattlefield(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Durkwood Boars");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotInGraveyard(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Returns a controlled permanent to its owner's hand")
    void returnsControlledPermanentToOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DurkwoodBoars());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Durkwood Boars");
        harness.assertInHand(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
