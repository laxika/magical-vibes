package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class ColdSnapTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private Permanent snowLand(Player controller) {
        Permanent snowLand = new Permanent(new Plains());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(controller.getId()).add(snowLand);
        return snowLand;
    }

    @Test
    @DisplayName("Deals damage to the active player equal to snow lands they control")
    void damagesActivePlayerBySnowLandCount() {
        harness.addToBattlefield(player1, new ColdSnap());
        snowLand(player2);
        snowLand(player2);
        snowLand(player2);

        // Opponent's upkeep: only the damage trigger fires (CU is controller-only)
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damages each player based on their own snow lands during their own upkeep")
    void damagesEachPlayerByOwnSnowLands() {
        harness.addToBattlefield(player1, new ColdSnap());
        snowLand(player2);
        snowLand(player2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals no damage when the active player controls no snow lands")
    void noDamageWithoutSnowLands() {
        harness.addToBattlefield(player1, new ColdSnap());
        // Non-snow land does not count
        harness.addToBattlefield(player2, new Plains());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Cold Snap")
    void paysCumulativeUpkeep() {
        Permanent snap = harness.addToBattlefieldAndReturn(player1, new ColdSnap());

        advanceToUpkeep(player1);
        // CU + EACH_UPKEEP damage both queue; drain stack until may-pay prompts
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(snap.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snap);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Cold Snap")
    void declineSacrifices() {
        Permanent snap = harness.addToBattlefieldAndReturn(player1, new ColdSnap());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snap);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Cold Snap"));
    }
}
