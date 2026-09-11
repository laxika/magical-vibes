package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AsForetold;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoilingVortex.class, AsForetold.class, GrizzlyBears.class})
class RoilingVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the active player during each upkeep")
    void damagesActivePlayerOnEachUpkeep() {
        harness.addToBattlefield(player1, new RoilingVortex());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals 5 damage when a player casts a spell without spending mana")
    void damagesPlayerCastingFreeSpell() {
        harness.addToBattlefield(player1, new RoilingVortex());
        var asForetold = harness.addToBattlefieldAndReturn(player2, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Does not trigger when a player casts a spell with mana")
    void doesNotDamagePlayerCastingPaidSpell() {
        harness.addToBattlefield(player1, new RoilingVortex());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The activated ability stops opponents from gaining life for the turn")
    void opponentsCantGainLifeThisTurn() {
        harness.addToBattlefield(player1, new RoilingVortex());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The activated ability's life-gain restriction wears off at end of turn")
    void lifeGainRestrictionWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RoilingVortex());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();

        new TurnCleanupService(null, null).resetEndOfTurnModifiers(gd);

        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
    }
}
