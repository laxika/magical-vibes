package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RishadanBrigandTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent may pay {3} to keep their permanents")
    void opponentMayPayToKeepPermanent() {
        harness.addToBattlefield(player2, new Millstone());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castRishadanBrigand();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Millstone");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent who declines sacrifices a permanent of their choice")
    void opponentDeclinesAndSacrificesPermanent() {
        harness.addToBattlefield(player2, new Millstone());
        castRishadanBrigand();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("An opponent chooses which permanent to sacrifice")
    void opponentChoosesPermanentToSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castRishadanBrigand();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("Can block flying creatures")
    void canBlockFlyingCreatures() {
        Permanent brigand = new Permanent(new RishadanBrigand());
        brigand.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(brigand);

        Permanent flyingAttacker = new Permanent(new AirElemental());
        flyingAttacker.setSummoningSick(false);
        flyingAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(flyingAttacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(brigand.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block non-flying creatures")
    void cannotBlockNonFlyingCreatures() {
        Permanent brigand = new Permanent(new RishadanBrigand());
        brigand.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(brigand);

        Permanent nonFlyingAttacker = new Permanent(new GrizzlyBears());
        nonFlyingAttacker.setSummoningSick(false);
        nonFlyingAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(nonFlyingAttacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    private void castRishadanBrigand() {
        harness.setHand(player1, List.of(new RishadanBrigand()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
    }
}
