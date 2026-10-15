package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
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

@CardUsed({CrosswayVampire.class, WalkingCorpse.class})
class CrosswayVampireTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);

        // Resolve ETB
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Target creature cannot declare as blocker after ETB resolves")
    void targetCannotDeclareAsBlocker() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering an empty battlefield requires targeting itself")
    void targetsItselfWhenNoOtherCreatures() {
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crossway Vampire");
        Permanent vampire = findPermanent(player1, "Crossway Vampire");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, vampire.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(vampire.getId());
        harness.passBothPriorities();

        assertThat(vampire.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("ETB can target a creature its controller controls and leaves others unaffected")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new WalkingCorpse());
        Permanent other = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(findPermanent(player1, "Crossway Vampire").isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("ETB resolves even if Crossway Vampire leaves before the ability resolves")
    void abilityResolvesAfterSourceLeaves() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Crossway Vampire"));
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The blocking restriction lasts through the end step and expires for the next turn")
    void restrictionExpiresAtCleanup() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CrosswayVampire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }
}
