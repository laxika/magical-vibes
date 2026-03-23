package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SacrificeAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DispenseJusticeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has correct effect type and needs target")
    void hasCorrectProperties() {
        DispenseJustice card = new DispenseJustice();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(SacrificeAttackingCreaturesEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Dispense Justice targeting a player puts it on the stack")
    void castingPutsOnStack() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Dispense Justice");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Without metalcraft: opponent with one attacking creature sacrifices it automatically")
    void withoutMetalcraftOneAttackerSacrificesAutomatically() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Without metalcraft: opponent with multiple attackers is prompted to choose one")
    void withoutMetalcraftMultipleAttackersPromptChoice() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingSacrificeAttackingCreature).isTrue();
    }

    @Test
    @DisplayName("Without metalcraft: opponent chooses which attacking creature to sacrifice")
    void withoutMetalcraftOpponentChooses() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player 2 chooses to sacrifice Grizzly Bears
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("With metalcraft: opponent sacrifices two attacking creatures")
    void withMetalcraftSacrificesTwoAttackers() {
        // Give player1 (caster) three artifacts for metalcraft
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Both creatures auto-sacrificed (eligible count == required count)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("With metalcraft and 3+ attackers: opponent chooses two to sacrifice")
    void withMetalcraftThreeAttackersChoosesTwo() {
        // Give player1 three artifacts for metalcraft
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        giant.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(spider);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);

        // Player 2 chooses to sacrifice Grizzly Bears and Hill Giant
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId(), giant.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    // ===== Metalcraft counts only controller's artifacts =====

    @Test
    @DisplayName("Metalcraft only counts caster's artifacts, not opponent's")
    void metalcraftOnlyCountsControllerArtifacts() {
        // Give opponent (player2) three artifacts — should NOT count for caster's metalcraft
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Without metalcraft, only 1 sacrifice required — so with 2 attackers, should prompt choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
    }

    // ===== Non-attacking creatures =====

    @Test
    @DisplayName("Does not affect non-attacking creatures")
    void doesNotAffectNonAttackingCreatures() {
        // One attacking, one not attacking
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent nonAttacker = new Permanent(new GiantSpider());
        nonAttacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        gd.playerBattlefields.get(player2.getId()).add(nonAttacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only the attacker is sacrificed (auto, since it's the only one eligible)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    // ===== No attacking creatures =====

    @Test
    @DisplayName("Does nothing if target player has no attacking creatures")
    void doesNothingIfNoAttackingCreatures() {
        // Non-attacking creature on battlefield
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no attacking creatures to sacrifice"));
    }

    // ===== Auto-sacrifice when fewer attackers than required =====

    @Test
    @DisplayName("With metalcraft and only one attacker: auto-sacrifices that one")
    void withMetalcraftOneAttackerAutoSacrifices() {
        // Give player1 three artifacts for metalcraft
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Even though metalcraft says 2, only 1 attacker exists — sacrificed automatically
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
