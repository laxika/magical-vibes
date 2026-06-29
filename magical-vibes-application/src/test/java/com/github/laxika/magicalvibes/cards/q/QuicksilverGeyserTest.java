package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuicksilverGeyserTest extends BaseCardTest {

    // ===== Bounce two targets =====

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

        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
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

        harness.castInstant(player1, 0, List.of(creatureId, artifactId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    // ===== Bounce one target =====

    @Test
    @DisplayName("Can target only one nonland permanent")
    void bouncesOneTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new QuicksilverGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Cannot target lands =====

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

    // ===== Partial fizzle =====

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

        GameData gd = harness.getGameData();
        // Spellbook should still be bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    // ===== Full fizzle =====

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
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Quicksilver Geyser"));
    }

    // ===== Can bounce own permanents =====

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

        harness.castInstant(player1, 0, List.of(creatureId, artifactId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    // ===== Bounce permanents from different players =====

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

        harness.castInstant(player1, 0, List.of(ownArtifactId, opponentCreatureId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
