package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriceOfKnowledge.class, Swamp.class, WitchbaneOrb.class})
class PriceOfKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to an opponent's hand size on that opponent's upkeep")
    void damagesOpponentEqualToHandSize() {
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player2, List.of(new Swamp(), new Swamp(), new Swamp()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Does not trigger during its controller's upkeep")
    void doesNotTriggerDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player1, List.of(new Swamp(), new Swamp(), new Swamp()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Allows players to keep more than seven cards during cleanup")
    void playersHaveNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player1, List.of(
                new Swamp(), new Swamp(), new Swamp(), new Swamp(),
                new Swamp(), new Swamp(), new Swamp(), new Swamp()));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    void opponentAlsoHasNoMaximumHandSize() {
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player2, List.of(
                new Swamp(), new Swamp(), new Swamp(), new Swamp(),
                new Swamp(), new Swamp(), new Swamp(), new Swamp()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(8);
    }

    @Test
    void damageUsesHandSizeAtResolution() {
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player2, List.of(new Swamp(), new Swamp(), new Swamp()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new Swamp()));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void emptyHandDealsNoDamage() {
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void triggerStillDealsDamageAfterSourceLeavesBattlefield() {
        var source = harness.addToBattlefieldAndReturn(player1, new PriceOfKnowledge());
        harness.setHand(player2, List.of(new Swamp(), new Swamp()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @CardUsed({PriceOfKnowledge.class, Swamp.class, WitchbaneOrb.class})
    void hexproofDoesNotStopUpkeepDamage() {
        harness.addToBattlefield(player1, new PriceOfKnowledge());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.setHand(player2, List.of(new Swamp(), new Swamp(), new Swamp()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }
}
