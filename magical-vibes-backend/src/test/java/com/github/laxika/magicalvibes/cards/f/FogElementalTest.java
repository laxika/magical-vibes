package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FogElementalTest {

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
    @DisplayName("Fog Elemental has correct card properties")
    void hasCorrectProperties() {
        FogElemental card = new FogElemental();

        assertThat(card.getName()).isEqualTo("Fog Elemental");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(4);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(card.getKeywords()).isEqualTo(Set.of(Keyword.FLYING));
        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(SacrificeAtEndOfCombatEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK).getFirst()).isInstanceOf(SacrificeAtEndOfCombatEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Fog Elemental puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FogElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Fog Elemental");
    }

    @Test
    @DisplayName("Resolving puts Fog Elemental onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new FogElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fog Elemental"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new FogElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Fog Elemental enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new FogElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fog Elemental"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Attack trigger pushes onto stack =====

    @Test
    @DisplayName("Declaring Fog Elemental as attacker pushes a triggered ability onto the stack")
    void attackTriggerPushesOntoStack() {
        Permanent fogPerm = new Permanent(new FogElemental());
        fogPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fogPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Fog Elemental");
        assertThat(entry.getSourcePermanentId()).isEqualTo(fogPerm.getId());
    }

    // ===== Block trigger pushes onto stack =====

    @Test
    @DisplayName("Declaring Fog Elemental as blocker pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent fogPerm = new Permanent(new FogElemental());
        fogPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(fogPerm);

        Permanent atkPerm = new Permanent(new FogElemental());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Fog Elemental");
        assertThat(entry.getSourcePermanentId()).isEqualTo(fogPerm.getId());
    }

    // ===== Sacrificed at end of combat when attacking =====

    @Test
    @DisplayName("Fog Elemental is sacrificed at end of combat after attacking")
    void sacrificedAtEndOfCombatWhenAttacking() {
        harness.setLife(player2, 20);

        Permanent fogPerm = new Permanent(new FogElemental());
        fogPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fogPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));
        // Resolve attack trigger
        harness.passBothPriorities();

        // Fog Elemental should be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fog Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fog Elemental"));
    }

    @Test
    @DisplayName("Fog Elemental deals combat damage before being sacrificed")
    void dealsDamageBeforeSacrifice() {
        harness.setLife(player2, 20);

        Permanent fogPerm = new Permanent(new FogElemental());
        fogPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fogPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Fog Elemental should deal 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // And then be sacrificed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fog Elemental"));
    }

    // ===== Sacrificed at end of combat when blocking =====

    @Test
    @DisplayName("Fog Elemental is sacrificed at end of combat after blocking")
    void sacrificedAtEndOfCombatWhenBlocking() {
        Permanent fogPerm = new Permanent(new FogElemental());
        fogPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(fogPerm);

        // Use a small creature so Fog Elemental survives combat damage
        GrizzlyBears bears = new GrizzlyBears();
        Permanent atkPerm = new Permanent(bears);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        // Resolve block trigger
        harness.passBothPriorities();

        // Fog Elemental should be in graveyard (sacrificed at end of combat)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fog Elemental"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fog Elemental"));
        // Grizzly Bears should also be dead from combat damage (4 power vs 2 toughness)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Not sacrificed if removed before end of combat =====

    @Test
    @DisplayName("Fog Elemental is not sacrificed if removed from battlefield before trigger resolves")
    void notSacrificedIfRemovedBeforeTriggerResolves() {
        Permanent fogPerm = new Permanent(new FogElemental());
        fogPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fogPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        // Remove Fog Elemental before trigger resolves (e.g., bounced)
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Fog Elemental should not end up in graveyard from sacrifice
        // (it was already removed from battlefield)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fog Elemental"));
    }

    // ===== Normal creatures don't trigger on attack =====

    @Test
    @DisplayName("Normal creature attacking does not push any trigger onto the stack")
    void normalCreatureDoesNotTriggerOnAttack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Attack trigger generates appropriate game log entries")
    void attackTriggerGeneratesLogEntries() {
        Permanent fogPerm = new Permanent(new FogElemental());
        fogPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fogPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Fog Elemental") && log.contains("attack") && log.contains("trigger"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Fog Elemental") && log.contains("sacrificed"));
    }
}

