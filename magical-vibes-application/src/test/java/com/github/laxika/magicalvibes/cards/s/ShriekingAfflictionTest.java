package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShriekingAffliction.class, WitchbaneOrb.class})
class ShriekingAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's upkeep with an empty hand costs them 3 life")
    void opponentUpkeepWithEmptyHandLosesLife() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Opponent's upkeep with exactly one card in hand costs them 3 life")
    void opponentUpkeepWithOneCardLosesLife() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of(new ShriekingAffliction()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Opponent's upkeep with two cards in hand does nothing")
    void opponentUpkeepWithTwoCardsDoesNothing() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of(new ShriekingAffliction(), new ShriekingAffliction()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Controller's own upkeep never triggers, even with an empty hand")
    void ownUpkeepDoesNothing() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Intervening-if is rechecked at resolution when the hand grows past one")
    void interveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of(new ShriekingAffliction()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new ShriekingAffliction(), new ShriekingAffliction()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Reducing a two-card hand after upkeep starts does not create a trigger")
    void handShrinkingAfterUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of(new ShriekingAffliction(), new ShriekingAffliction()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Two copies each make the opponent lose 3 life")
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player2, lifeBefore - 6);
        harness.assertLife(player1, controllerLifeBefore);
    }

    @Test
    @DisplayName("The upkeep trigger resolves after its source leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of(new ShriekingAffliction()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        var source = findPermanent(player1, "Shrieking Affliction");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.setHand(player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
    }

    @Test
    @CardUsed({ShriekingAffliction.class, WitchbaneOrb.class})
    @DisplayName("Player hexproof does not stop the non-targeting upkeep ability")
    void opponentWithHexproofStillLosesLife() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
    }
}
