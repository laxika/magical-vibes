package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssassinateTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Assassinate targeting a tapped creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, tappedCreature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Assassinate");
        assertThat(entry.getTargetId()).isEqualTo(tappedCreature.getId());
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        // Add a tapped creature as valid target so spell is playable
        Permanent tappedValid = new Permanent(new GrizzlyBears());
        tappedValid.tap();
        harness.getGameData().playerBattlefields.get(player1.getId()).add(tappedValid);

        Permanent untappedCreature = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(untappedCreature);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature")
    void cannotTargetTappedNonCreature() {
        // Add a tapped creature as valid target so spell is playable
        Permanent tappedValid = new Permanent(new GrizzlyBears());
        tappedValid.tap();
        harness.getGameData().playerBattlefields.get(player1.getId()).add(tappedValid);

        Permanent tappedLand = new Permanent(new Forest());
        tappedLand.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedLand);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, tappedLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving destroys target tapped creature")
    void resolvingDestroysTargetTappedCreature() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, tappedCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy own tapped creature")
    void canDestroyOwnTappedCreature() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player1.getId()).add(tappedCreature);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, tappedCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Assassinate goes to graveyard after resolving")
    void assassinateGoesToGraveyardAfterResolving() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, tappedCreature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Assassinate");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, tappedCreature.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Assassinate still goes to graveyard
        harness.assertInGraveyard(player1, "Assassinate");
    }

    @Test
    @DisplayName("Fizzles if target creature becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntapped() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, tappedCreature.getId());

        // Untap the target before resolution
        tappedCreature.untap();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Spell fizzles — creature survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Assassinate still goes to graveyard
        harness.assertInGraveyard(player1, "Assassinate");
    }
}

