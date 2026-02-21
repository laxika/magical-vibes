package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RhoxTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Rhox has correct card properties")
    void hasCorrectProperties() {
        Rhox card = new Rhox();

        assertThat(card.getName()).isEqualTo("Rhox");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{4}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(5);
        assertThat(card.getToughness()).isEqualTo(5);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.RHINO, CardSubtype.BEAST);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(AssignCombatDamageAsThoughUnblockedEffect.class);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{2}{G}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(RegenerateEffect.class);
    }

    @Test
    @DisplayName("Blocked Rhox assigns combat damage to defending player")
    void blockedRhoxAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Rhox());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent rhox = gd.playerBattlefields.get(player1.getId()).getFirst();
        rhox.setSummoningSick(false);
        rhox.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE (paused for assignment)
        harness.passBothPriorities();

        // Rhox has "assign as though unblocked" — assign all damage to defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 5));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Rhox regeneration ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        harness.addToBattlefield(player1, new Rhox());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        Permanent rhox = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(rhox.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration saves Rhox from lethal combat damage while still damaging player")
    void regenerationSavesFromLethalCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Rhox());
        harness.addToBattlefield(player2, new ShivanDragon());

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent rhox = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(rhox.getRegenerationShield()).isEqualTo(1);

        rhox.setSummoningSick(false);
        rhox.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE (paused for assignment)
        harness.passBothPriorities();

        // Rhox has "assign as though unblocked" — assign all damage to defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 5));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rhox"));
        Permanent survivingRhox = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rhox"))
                .findFirst()
                .orElseThrow();
        assertThat(survivingRhox.getRegenerationShield()).isZero();
        assertThat(survivingRhox.isTapped()).isTrue();
    }
}
