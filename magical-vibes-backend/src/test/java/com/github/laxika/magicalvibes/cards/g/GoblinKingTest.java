package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinKingTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Goblin King has correct card properties")
    void hasCorrectProperties() {
        GoblinKing card = new GoblinKing();

        assertThat(card.getName()).isEqualTo("Goblin King");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(BoostCreaturesBySubtypeEffect.class);

        BoostCreaturesBySubtypeEffect effect = (BoostCreaturesBySubtypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.affectedSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).containsExactly(Keyword.MOUNTAINWALK);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Goblin King puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GoblinKing()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin King");
    }

    @Test
    @DisplayName("Resolving puts Goblin King onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GoblinKing()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin King"));
    }

    // ===== Static effect: buffs other Goblins =====

    @Test
    @DisplayName("Other Goblin creatures get +1/+1 and mountainwalk")
    void buffsOtherGoblins() {
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player1, new GoblinKing());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Goblin King does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new GoblinKing());

        Permanent king = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin King"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, king)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, king)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, king, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Goblin creatures")
    void doesNotBuffNonGoblins() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinKing());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Buffs opponent's Goblin creatures too")
    void buffsOpponentGoblins() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());

        Permanent opponentGoblin = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentGoblin, Keyword.MOUNTAINWALK)).isTrue();
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Goblin Kings buff each other")
    void twoKingsBuffEachOther() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GoblinKing());

        List<Permanent> kings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin King"))
                .toList();

        assertThat(kings).hasSize(2);
        for (Permanent king : kings) {
            assertThat(gqs.getEffectivePower(gd, king)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, king)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, king, Keyword.MOUNTAINWALK)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Goblin Kings give +2/+2 to other Goblins")
    void twoKingsStackBonuses() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        // 2/2 base + 2/2 from two kings = 4/4
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Goblin King leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Goblin King"));

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Goblin King resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.setHand(player1, List.of(new GoblinKing()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        goblin.setPowerModifier(goblin.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(8); // 2 base + 5 spell + 1 static

        goblin.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isTrue();
    }

    // ===== Mountainwalk blocking =====

    @Test
    @DisplayName("Goblin with mountainwalk cannot be blocked when defender controls a Mountain")
    void mountainwalkPreventsBlockingWhenDefenderControlsMountain() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player2, new Mountain());

        Permanent goblinAttacker = new Permanent(new GoblinEliteInfantry());
        goblinAttacker.setSummoningSick(false);
        goblinAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(goblinAttacker);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(goblinAttacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Goblin with mountainwalk can be blocked when defender does not control a Mountain")
    void mountainwalkAllowsBlockingWithoutMountain() {
        harness.addToBattlefield(player1, new GoblinKing());

        Permanent goblinAttacker = new Permanent(new GoblinEliteInfantry());
        goblinAttacker.setSummoningSick(false);
        goblinAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(goblinAttacker);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(goblinAttacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Goblin King itself does not have mountainwalk (it only grants it)")
    void goblinKingDoesNotHaveMountainwalkItself() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player2, new Mountain());

        Permanent king = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin King"))
                .findFirst().orElseThrow();
        king.setSummoningSick(false);
        king.setAttacking(true);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(king);

        // Goblin King alone doesn't buff itself, so it has no mountainwalk and can be blocked
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk is lost when Goblin King leaves the battlefield")
    void mountainwalkLostWhenKingLeaves() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player2, new Mountain());

        Permanent goblinAttacker = new Permanent(new GoblinEliteInfantry());
        goblinAttacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(goblinAttacker);

        // Verify mountainwalk is present
        assertThat(gqs.hasKeyword(gd, goblinAttacker, Keyword.MOUNTAINWALK)).isTrue();

        // Remove Goblin King
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Goblin King"));

        // Mountainwalk should be gone, allowing blocking
        assertThat(gqs.hasKeyword(gd, goblinAttacker, Keyword.MOUNTAINWALK)).isFalse();
    }
}


