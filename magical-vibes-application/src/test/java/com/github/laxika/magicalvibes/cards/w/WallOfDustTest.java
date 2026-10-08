package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AnimateArtifact;
import com.github.laxika.magicalvibes.cards.c.Conservator;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfDust.class, GrizzlyBears.class, AnimateArtifact.class, Conservator.class, Disenchant.class})
class WallOfDustTest extends BaseCardTest {

    private void advanceTurn(Player nextActivePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }

    @Test
    @DisplayName("Defender prevents Wall of Dust from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new WallOfDust());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When Wall of Dust blocks an attacker, its trigger flags that attacker for next turn")
    void blockingFlagsAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfDust());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getSourcePermanentId().equals(wall.getId())
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(attacker.isCantAttackNextTurn()).isTrue();
    }

    @Test
    @DisplayName("Each creature blocked by Wall of Dust is restricted independently")
    void eachBlockedCreatureIsRestrictedIndependently() {
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WallOfDust());
        addCreatureReady(player2, new WallOfDust());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        resolveAllTriggers();

        assertThat(firstAttacker.isCantAttackNextTurn()).isTrue();
        assertThat(secondAttacker.isCantAttackNextTurn()).isTrue();
    }

    @Test
    @DisplayName("The restriction arms only on the creature's controller's next turn, not the intervening opponent turn")
    void restrictionArmsOnControllersNextTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setCantAttackNextTurn(true); // state produced by the block trigger

        // The intervening opponent turn must not arm the restriction.
        harness.forceActivePlayer(player1);
        advanceTurn(player2);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(bear.isCantAttackThisTurn()).isFalse();
        assertThat(bear.isCantAttackNextTurn()).isTrue();

        // The controller's next turn arms it and it can't be declared as an attacker.
        advanceTurn(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(bear.isCantAttackThisTurn()).isTrue();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The restriction wears off after the one turn and the creature can attack again")
    void restrictionWearsOff() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setCantAttackThisTurn(true); // already armed for player1's current turn
        harness.forceActivePlayer(player1);

        // player1 -> player2 -> player1: the following controller turn clears the restriction.
        advanceTurn(player2);
        advanceTurn(player1);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(bear.isCantAttackThisTurn()).isFalse();

        // The restriction is gone, so the creature is a legal attacker again.
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("A resolved block trigger prevents attacking for exactly the controller's next turn")
    void blockRestrictionLastsForExactlyNextTurn() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WallOfDust());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        advanceTurn(player2);
        advanceTurn(player1);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(1).doesNotContain(0);

        advanceTurn(player2);
        advanceTurn(player1);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0, 1);
    }

    @Test
    @DisplayName("The restriction applies even if the blocked permanent stops being a creature before resolution")
    void restrictionSurvivesLosingCreatureTypeBeforeResolution() {
        Permanent artifact = addCreatureReady(player1, new Conservator());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AnimateArtifact());
        aura.setAttachedTo(artifact.getId());
        addCreatureReady(player2, new WallOfDust());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();

        advanceTurn(player2);
        advanceTurn(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AnimateArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).doesNotContain(0);
    }
}
