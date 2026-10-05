package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksilverGeyser.class, GrizzlyBears.class, Island.class, Spellbook.class})
class QuicksilverGeyserTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves with zero targets without returning any permanents")
    void resolvesWithZeroTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Quicksilver Geyser");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot choose more than two targets")
    void rejectsThreeTargets() {
        UUID first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID third = harness.addToBattlefieldAndReturn(player2, new Spellbook()).getId();
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first, second, third)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 2 targets");
    }

    @Test
    @DisplayName("Cannot choose the same permanent twice")
    void rejectsDuplicateTargets() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(targetId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Returns a permanent to its owner rather than its current controller")
    void returnsToOwnerHand() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, bears).getId();
        gd.stolenCreatures.put(targetId, player1.getId());
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bounces two target creatures to their owners' hands")
    void bouncesTwoCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<UUID> targetIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, targetIds);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Bounces two different permanent types (creature + artifact)")
    void bouncesCreatureAndArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(creatureId, artifactId));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Spellbook");
    }

    @Test
    @DisplayName("Can target only one nonland permanent")
    void bouncesOneTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID landId = harness.getPermanentId(player2, "Island");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(landId, creatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Still bounces surviving target when one target is removed before resolution")
    void partialFizzle() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(creatureId, artifactId));

        // Remove one target before resolution
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Spellbook should still be bounced
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player2, "Spellbook");
    }

    @Test
    @DisplayName("Fizzles if all targets are removed before resolution")
    void fizzlesIfAllTargetsRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(creatureId, artifactId));

        // Remove all targets before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Quicksilver Geyser");
    }

    @Test
    @DisplayName("Can bounce own permanents")
    void canBounceOwnPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID artifactId = harness.getPermanentId(player1, "Spellbook");
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(creatureId, artifactId));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("Can target permanents controlled by different players")
    void bouncesFromDifferentPlayers() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownArtifactId = harness.getPermanentId(player1, "Spellbook");
        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(ownArtifactId, opponentCreatureId));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Spellbook");
        harness.assertInHand(player2, "Grizzly Bears");
    }
}
