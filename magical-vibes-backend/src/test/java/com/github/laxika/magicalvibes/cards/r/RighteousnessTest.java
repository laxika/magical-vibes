package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RighteousnessTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Righteousness has correct card properties")
    void hasCorrectProperties() {
        Righteousness card = new Righteousness();

        assertThat(card.getName()).isEqualTo("Righteousness");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(BoostTargetBlockingCreatureEffect.class);
        BoostTargetBlockingCreatureEffect effect = (BoostTargetBlockingCreatureEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(7);
        assertThat(effect.toughnessBoost()).isEqualTo(7);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Righteousness targeting a blocking creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, "W", 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Righteousness");
        assertThat(entry.getTargetPermanentId()).isEqualTo(blockerPerm.getId());
    }

    @Test
    @DisplayName("Cannot target a non-blocking creature")
    void cannotTargetNonBlockingCreature() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Righteousness()));
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Righteousness()));
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        gd.playerBattlefields.get(player1.getId()).add(blockerPerm);

        harness.setHand(player1, List.of(new Righteousness()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blockerPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving gives +7/+7 to target blocking creature")
    void resolvingGivesBoost() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, "W", 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        harness.passBothPriorities();

        assertThat(blockerPerm.getEffectivePower()).isEqualTo(9);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(9);
        assertThat(blockerPerm.getPowerModifier()).isEqualTo(7);
        assertThat(blockerPerm.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, "W", 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        harness.passBothPriorities();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blockerPerm.getPowerModifier()).isEqualTo(0);
        assertThat(blockerPerm.getToughnessModifier()).isEqualTo(0);
        assertThat(blockerPerm.getEffectivePower()).isEqualTo(2);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Righteousness goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, "W", 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Righteousness"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Righteousness fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, "W", 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Combat interaction =====

    @Test
    @DisplayName("Boosted blocker survives combat with a large attacker")
    void boostedBlockerSurvivesCombat() {
        harness.setLife(player2, 20);

        // Player1 has a 5/5 attacker
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(5);
        bigCreature.setToughness(5);
        Permanent atkPerm = new Permanent(bigCreature);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        // Player2 has a 2/2 blocker — set up blocking state manually
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        // Cast and resolve Righteousness before combat damage
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, "W", 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        harness.passBothPriorities();

        // Blocker is now 9/9 — verify boost applied
        assertThat(blockerPerm.getEffectivePower()).isEqualTo(9);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(9);

        // Advance to combat damage
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Blocker should survive (9 toughness vs 5 damage), attacker should die (5 toughness vs 9 damage)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Player2 takes no damage (attacker was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Can target opponent's blocking creature =====

    @Test
    @DisplayName("Can target opponent's blocking creature")
    void canTargetOpponentsBlockingCreature() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Righteousness()));
        harness.addMana(player1, "W", 1);

        harness.castInstant(player1, 0, blockerPerm.getId());
        harness.passBothPriorities();

        assertThat(blockerPerm.getEffectivePower()).isEqualTo(9);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(9);
    }
}
