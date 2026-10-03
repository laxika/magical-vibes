package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DispenseJustice.class, GrizzlyBears.class, GiantSpider.class, HillGiant.class, Spellbook.class})
class DispenseJusticeTest extends BaseCardTest {

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
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Without metalcraft: opponent with one attacking creature sacrifices it automatically")
    void withoutMetalcraftOneAttackerSacrificesAutomatically() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without metalcraft: opponent with multiple attackers is prompted to choose one")
    void withoutMetalcraftMultipleAttackersPromptChoice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).context())
                .isInstanceOf(MultiPermanentChoiceContext.SacrificeAttackingCreatures.class);
    }

    @Test
    @DisplayName("Without metalcraft: opponent chooses which attacking creature to sacrifice")
    void withoutMetalcraftOpponentChooses() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Player 2 chooses to sacrifice Grizzly Bears
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With metalcraft: opponent sacrifices two attacking creatures")
    void withMetalcraftSacrificesTwoAttackers() {
        // Give player1 (caster) three artifacts for metalcraft
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Both creatures auto-sacrificed (eligible count == required count)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("With metalcraft and 3+ attackers: opponent chooses two to sacrifice")
    void withMetalcraftThreeAttackersChoosesTwo() {
        // Give player1 three artifacts for metalcraft
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setSummoningSick(false);
        giant.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        // Player 2 chooses to sacrifice Grizzly Bears and Hill Giant
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId(), giant.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Metalcraft only counts caster's artifacts, not opponent's")
    void metalcraftOnlyCountsControllerArtifacts() {
        // Give opponent (player2) three artifacts — should NOT count for caster's metalcraft
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setSummoningSick(false);
        spider.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Without metalcraft, only 1 sacrifice required — so with 2 attackers, should prompt choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("Does not affect non-attacking creatures")
    void doesNotAffectNonAttackingCreatures() {
        // One attacking, one not attacking
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        nonAttacker.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Only the attacker is sacrificed (auto, since it's the only one eligible)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

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

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no attacking creatures to sacrifice"));
    }

    @Test
    @DisplayName("With metalcraft and only one attacker: auto-sacrifices that one")
    void withMetalcraftOneAttackerAutoSacrifices() {
        // Give player1 three artifacts for metalcraft
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Even though metalcraft says 2, only 1 attacker exists — sacrificed automatically
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
    @Test
    void cannotDeclineRequiredSacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void metalcraftCannotChooseOnlyOneOfThreeAttackers() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setAttacking(true);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId(), giant.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    void chosenSacrificeRecordsThatPlayerSacrificedThisTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn).contains(player2.getId());
    }

    @Test
    void gainingMetalcraftBeforeResolutionIncreasesSacrificeCount() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new Spellbook());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }
}
