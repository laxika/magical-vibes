package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.m.MossDiamond;
import com.github.laxika.magicalvibes.cards.p.PrismaticCircle;
import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Boomerang.class, BayFalcon.class, PrismaticCircle.class, Island.class, MossDiamond.class})
class BoomerangTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Boomerang puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new BayFalcon());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Bay Falcon");
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
        harness.addToBattlefield(player2, new BayFalcon());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Bay Falcon");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Bay Falcon");
        harness.assertInHand(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("Resolving returns target enchantment to owner's hand")
    void resolvingReturnsEnchantmentToHand() {
        harness.addToBattlefield(player2, new PrismaticCircle());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Prismatic Circle");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Prismatic Circle");
        harness.assertInHand(player2, "Prismatic Circle");
    }

    @Test
    @DisplayName("Resolving returns target land to owner's hand")
    void resolvingReturnsLandToHand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Can bounce own permanent")
    void canBounceOwnPermanent() {
        harness.addToBattlefield(player1, new BayFalcon());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player1, "Bay Falcon");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        harness.assertInHand(player1, "Bay Falcon");
    }

    @Test
    @DisplayName("Resolving returns target artifact to owner's hand")
    void resolvingReturnsArtifactToHand() {
        harness.addToBattlefield(player2, new MossDiamond());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Moss Diamond");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Moss Diamond");
        harness.assertInHand(player2, "Moss Diamond");
    }

    @Test
    @DisplayName("Boomerang goes to graveyard after resolving")
    void boomerangGoesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new BayFalcon());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Bay Falcon");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Boomerang");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Bay Falcon");
        // Boomerang still goes to graveyard
        harness.assertInGraveyard(player1, "Boomerang");
    }

    @Test
    @DisplayName("Target creature does not go to graveyard (it goes to hand)")
    void targetDoesNotGoToGraveyard() {
        harness.addToBattlefield(player2, new BayFalcon());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Bay Falcon");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotInGraveyard(player2, "Bay Falcon");
    }
}

