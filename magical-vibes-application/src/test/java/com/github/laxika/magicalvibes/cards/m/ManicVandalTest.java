package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.c.CrystalBall;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManicVandal.class, CrystalBall.class, RuneclawBear.class, Ornithopter.class})
class ManicVandalTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Manic Vandal can preselect its ETB target")
    void castingCanPreselectEtbTarget() {
        harness.addToBattlefield(player2, new CrystalBall());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Crystal Ball");
        harness.castCreature(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Manic Vandal");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Manic Vandal enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new CrystalBall());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Crystal Ball");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Manic Vandal");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Manic Vandal");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new CrystalBall());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Crystal Ball");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Crystal Ball");
        harness.assertInGraveyard(player2, "Crystal Ball");
    }

    @Test
    @DisplayName("Can destroy own artifact with ETB")
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new CrystalBall());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player1, "Crystal Ball");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crystal Ball");
        harness.assertInGraveyard(player1, "Crystal Ball");
    }

    @Test
    @DisplayName("ETB fizzles if target artifact is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new CrystalBall());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Crystal Ball");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast without a target when no artifacts on battlefield")
    void canCastWithoutTargetWhenNoArtifacts() {
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Manic Vandal");
    }

    @Test
    @DisplayName("No ETB ability remains on the stack when no legal artifact target exists")
    void noEtbOnStackWhenNoLegalTargets() {
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature should be on battlefield
        harness.assertOnBattlefield(player1, "Manic Vandal");
        // No legal targets, so the triggered ability is removed from the stack.
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new CrystalBall());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Crystal Ball");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
    @Test
    @DisplayName("Can cast without preselecting a target and choose an artifact after entry")
    void choosesArtifactAfterEntering() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Manic Vandal");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Ornithopter"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact appearing while the creature spell is on the stack can be targeted")
    void targetsArtifactThatAppearsBeforeEntry() {
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);

        harness.addToBattlefield(player2, new CrystalBall());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Crystal Ball"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Crystal Ball");
        harness.assertOnBattlefield(player1, "Manic Vandal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB ability still destroys its target after Manic Vandal leaves")
    void etbResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new CrystalBall());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Crystal Ball"));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Crystal Ball");
        assertThat(gd.stack).isEmpty();
    }
}
