package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.k.KingCheetah;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.u.UrborgMindsucker;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nekrataal.class, KingCheetah.class, PhyrexianWalker.class, UrborgMindsucker.class})
class NekrataalTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Nekrataal puts it on the stack with target")
    void castingPutsItOnStackWithTarget() {
        harness.addToBattlefield(player2, new KingCheetah());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "King Cheetah");
        harness.castCreature(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Nekrataal enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new KingCheetah());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "King Cheetah");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Nekrataal");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target creature")
    void etbDestroysTargetCreature() {
        harness.addToBattlefield(player2, new KingCheetah());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "King Cheetah");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "King Cheetah");
        harness.assertInGraveyard(player2, "King Cheetah");
    }

    @Test
    @DisplayName("Can target a nonblack nonartifact creature you control")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KingCheetah());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent == target);
        harness.assertOnBattlefield(player1, "Nekrataal");
        harness.assertInGraveyard(player1, "King Cheetah");
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new UrborgMindsucker());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Urborg Mindsucker");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new PhyrexianWalker());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Phyrexian Walker");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact");
    }

    // ===== Regeneration bypass =====

    @Test
    @DisplayName("Destroyed creature cannot be regenerated")
    void destroyedCreatureCannotBeRegenerated() {
        harness.addToBattlefield(player2, new KingCheetah());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "King Cheetah");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Give the target a regeneration shield before ETB resolves
        Permanent target = findPermanent(player2, "King Cheetah");
        target.setRegenerationShield(1);

        // Resolve ETB — should destroy despite regeneration shield
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "King Cheetah");
        harness.assertInGraveyard(player2, "King Cheetah");
    }

    // ===== Indestructible =====

    @Test
    @DisplayName("Indestructible creature survives Nekrataal's ETB")
    void indestructibleCreatureSurvives() {
        harness.addToBattlefield(player2, new KingCheetah());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "King Cheetah");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Grant indestructible to the target before ETB resolves
        Permanent target = findPermanent(player2, "King Cheetah");
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        // Resolve ETB — should not destroy indestructible creature
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "King Cheetah");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("indestructible"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new KingCheetah());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "King Cheetah");
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

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no valid creatures on battlefield")
    void canCastWithoutTargetWhenNoValidCreatures() {
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Nekrataal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast without target when only black creatures exist")
    void canCastWithoutTargetWhenOnlyBlackCreatures() {
        harness.addToBattlefield(player2, new UrborgMindsucker());
        harness.castFromHand(player1, new Nekrataal(), "{2}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Keywords =====

    @Test
    @DisplayName("First strike lets Nekrataal survive a lethal block")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new Nekrataal());
        Permanent blocker = addCreatureReady(player2, new KingCheetah());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    // ===== Mana validation =====

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new KingCheetah());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "King Cheetah");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}

