package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BandageTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Bandage has correct card properties")
    void hasCorrectProperties() {
        Bandage card = new Bandage();

        assertThat(card.getName()).isEqualTo("Bandage");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getSpellEffects()).hasSize(2);
        assertThat(card.getSpellEffects().get(0)).isInstanceOf(PreventDamageToTargetEffect.class);
        assertThat(card.getSpellEffects().get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Bandage puts it on the stack")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, "W", 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Bandage");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast Bandage without enough mana")
    void cannotCastWithoutMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Prevention on creature =====

    @Test
    @DisplayName("Resolving Bandage adds prevention shield to target creature")
    void resolvingAddsPrevention() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, "W", 1);

        // Ensure player1 has a card to draw
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevention shield prevents 1 combat damage to creature")
    void preventionShieldPrevents1CombatDamage() {
        // Set up: player1's Grizzly Bears (2/2) with 1 prevention shield
        // vs player2's Grizzly Bears (2/2) attacking
        GrizzlyBears bear1 = new GrizzlyBears();
        Permanent defender = new Permanent(bear1);
        defender.setSummoningSick(false);
        defender.setDamagePreventionShield(1);
        defender.setBlocking(true);
        defender.setBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(defender);

        GrizzlyBears bear2 = new GrizzlyBears();
        Permanent attacker = new Permanent(bear2);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Passing priorities advances from DECLARE_BLOCKERS → COMBAT_DAMAGE, triggering resolveCombatDamage
        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        GameData gd = harness.getGameData();
        // Attacker dealt 2 damage, but defender has 1 prevention → 1 effective damage
        // 1 < 2 toughness → defender survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Attacker took 2 damage with no shield → 2 >= 2 toughness → attacker dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Prevention shield was consumed
        Permanent surviving = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(surviving.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Prevention shield is consumed after preventing damage")
    void preventionShieldIsConsumed() {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent target = new Permanent(bear);
        target.setSummoningSick(false);
        target.setDamagePreventionShield(1);
        target.setBlocking(true);
        target.setBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        GrizzlyBears attBear = new GrizzlyBears();
        Permanent attacker = new Permanent(attBear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        // After combat, shield should be consumed
        Permanent surviving = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(surviving.getDamagePreventionShield()).isEqualTo(0);
    }

    // ===== Prevention on player =====

    @Test
    @DisplayName("Resolving Bandage targeting a player adds prevention shield")
    void resolvingAddsPlayerPrevention() {
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, "W", 1);
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());

        // Target player2 with Bandage (using player UUID as target)
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Player prevention shield reduces combat damage by 1")
    void playerPreventionReducesCombatDamage() {
        harness.setLife(player2, 20);
        harness.getGameData().playerDamagePreventionShields.put(player2.getId(), 1);

        // Set up an unblocked 3/3 attacker
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        GameData gd = harness.getGameData();
        // 2 damage - 1 prevented = 1 effective damage → 20 - 1 = 19
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    // ===== Draw a card =====

    @Test
    @DisplayName("Resolving Bandage draws a card for the caster")
    void resolvingDrawsACard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears deckCard = new GrizzlyBears();
        harness.getGameData().playerDecks.get(player1.getId()).add(deckCard);

        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, "W", 1);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Hand should be empty after casting (Bandage was the only card)
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        // After resolving, player should have drawn a card
        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Bandage goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, "W", 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bandage"));
    }

    // ===== Prevention with spell damage =====

    @Test
    @DisplayName("Prevention shield reduces activated ability damage to creature")
    void preventionReducesAbilityDamage() {
        // Set up target creature with prevention shield
        GrizzlyBears bear = new GrizzlyBears();
        Permanent target = new Permanent(bear);
        target.setSummoningSick(false);
        target.setAttacking(true);
        target.setDamagePreventionShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        // Set up Ballista Squad for player1
        BallistaSquad ballista = new BallistaSquad();
        Permanent ballistaPerm = new Permanent(ballista);
        ballistaPerm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(ballistaPerm);

        harness.addMana(player1, "W", 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        // Activate ability with X=2, targeting the shielded creature
        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 2 damage - 1 prevented = 1 effective damage. 1 < 2 toughness → survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Bandage fizzles entirely if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, "W", 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Entire spell fizzles — no draw happens
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Fizzled spell still goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bandage"));
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Prevention shields are cleared at end of turn")
    void preventionShieldsClearedAtEndOfTurn() {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        perm.setDamagePreventionShield(1);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        harness.getGameData().playerDamagePreventionShields.put(player1.getId(), 1);

        harness.forceStep(TurnStep.CLEANUP);

        // Trigger cleanup by advancing through the step
        // Directly test resetEndOfTurnModifiers by moving past cleanup
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent afterCleanup = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(afterCleanup.getDamagePreventionShield()).isEqualTo(0);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }
}
