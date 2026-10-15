package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.t.TrumpetBlast;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinBattleJester.class, CanyonMinotaur.class, WalkingCorpse.class, TrumpetBlast.class})
class GoblinBattleJesterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a red spell triggers target selection")
    void redSpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new GoblinBattleJester());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CanyonMinotaur()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Chosen creature can't block this turn once the trigger resolves")
    void chosenCreatureCantBlock() {
        harness.addToBattlefield(player1, new GoblinBattleJester());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CanyonMinotaur()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        UUID blockerId = blocker.getId();
        harness.handlePermanentChosen(player1, blockerId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Affected creature cannot be declared as a blocker")
    void affectedCreatureCannotBeDeclaredAsBlocker() {
        harness.addToBattlefield(player1, new GoblinBattleJester());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CanyonMinotaur()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a nonred spell does not trigger the ability")
    void nonRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GoblinBattleJester());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting a red spell does not trigger the ability")
    void opponentRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GoblinBattleJester());
        harness.addToBattlefield(player1, new WalkingCorpse());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CanyonMinotaur()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Casting the Jester itself does not trigger its own ability")
    void doesNotTriggerOnItsOwnCast() {
        harness.setHand(player1, List.of(new GoblinBattleJester()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblin Battle Jester");
    }

    @Test
    @DisplayName("A red instant can target the Jester itself and only affects the chosen creature")
    void redInstantCanTargetSource() {
        Permanent jester = harness.addToBattlefieldAndReturn(player1, new GoblinBattleJester());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TrumpetBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, jester.getId());

        assertThat(jester.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(jester.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("The blocking restriction expires when the turn ends")
    void blockingRestrictionExpires() {
        harness.addToBattlefield(player1, new GoblinBattleJester());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TrumpetBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }
}
