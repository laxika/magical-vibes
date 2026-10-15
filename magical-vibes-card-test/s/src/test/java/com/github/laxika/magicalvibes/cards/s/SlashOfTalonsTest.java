package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.cards.h.HeadwaterSentries;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlashOfTalons.class, BishopsSoldier.class, HeadwaterSentries.class})
class SlashOfTalonsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Slash of Talons targeting an attacking creature puts it on the stack")
    void castingTargetingAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Casting Slash of Talons targeting a blocking creature puts it on the stack")
    void castingTargetingBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BishopsSoldier());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blocker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier()).getId();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    @Test
    @DisplayName("Resolving deals 2 damage, killing a 2-toughness creature")
    void resolvingKills2ToughnessCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Bishop's Soldier is 2/2, 2 damage kills it
        harness.assertNotOnBattlefield(player1, "Bishop's Soldier");
        harness.assertInGraveyard(player1, "Bishop's Soldier");
    }

    @Test
    @DisplayName("Resolving deals 2 damage to a creature with enough toughness to survive")
    void resolvingDeals2DamageToToughCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HeadwaterSentries());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Headwater Sentries survives 2 damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Headwater Sentries") && p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Slash of Talons goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Slash of Talons");
    }

    @Test
    @DisplayName("Slash of Talons fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Slash of Talons still goes to graveyard
        harness.assertInGraveyard(player2, "Slash of Talons");
    }

    @Test
    @DisplayName("Resolving kills a blocking creature with 2 toughness")
    void resolvingKillsBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BishopsSoldier());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SlashOfTalons()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bishop's Soldier");
        harness.assertInGraveyard(player2, "Bishop's Soldier");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Target leaving combat before resolution receives no damage")
    void targetLeavingCombatMakesSpellFizzle() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HeadwaterSentries());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SlashOfTalons()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Headwater Sentries");
        harness.assertInGraveyard(player2, "Slash of Talons");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
