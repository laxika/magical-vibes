package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackVise.class, GrizzlyBears.class, AvariceTotem.class, IvoryMask.class, Disenchant.class})
class BlackViseTest extends BaseCardTest {

    private List<Card> bears(int count) {
        return Stream.generate(GrizzlyBears::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Deals (hand size - 4) damage during the opponent's upkeep")
    void dealsScalingDamage() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(6)); // 6 - 4 = 2 damage
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has exactly four cards in hand")
    void noDamageWithFourCards() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(4)); // 4 - 4 = 0
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has fewer than four cards in hand")
    void noDamageWithFewerCards() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(1)); // 1 - 4 = -3, clamps to 0
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger during the controller's own upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player1, bears(7)); // full hand, but controller is unaffected
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Damage is recomputed from the hand size at resolution")
    void amountRecomputedAtResolution() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(6)); // would be 2 at trigger time
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        // Grow the hand while the trigger is on the stack: 8 - 4 = 4 at resolution.
        harness.setHand(player2, bears(8));
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void chosenOpponentStaysFixedWhenControlChanges() {
        var blackVise = harness.enterBattlefieldAndReturn(player1, new BlackVise());
        harness.addToBattlefield(player2, new AvariceTotem());
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.activateAbility(player2, 0, null, blackVise.getId());
        harness.passBothPriorities();

        harness.setHand(player2, bears(6));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Choosing an opponent as Black Vise enters does not create a triggered ability")
    void choosesOpponentAsItEntersWithoutUsingTheStack() {
        harness.castFromHand(player1, new BlackVise(), "{1}");
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player2.getId());
        }

        harness.assertOnBattlefield(player1, "Black Vise");
        assertThat(gd.stack).isEmpty();
        harness.setHand(player2, bears(6));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The upkeep ability damages the chosen player even if that player has shroud")
    void upkeepDamageDoesNotTargetTheChosenPlayer() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setHand(player2, bears(6));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals no damage when the chosen player has an empty hand")
    void noDamageWithEmptyHand() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals no damage if the hand shrinks below five cards before resolution")
    void noDamageWhenHandShrinksBeforeResolution() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(7));

        advanceToUpkeep(player2);
        harness.setHand(player2, bears(3));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An upkeep ability still deals damage after Black Vise is destroyed")
    void upkeepDamageSurvivesSourceRemoval() {
        var blackVise = harness.addToBattlefieldAndReturn(player1, new BlackVise());
        harness.setHand(player2, bears(6));
        harness.setHand(player1, List.of(new Disenchant()));

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP,
                () -> advanceToUpkeep(player2));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, blackVise.getId());
        harness.assertNotOnBattlefield(player1, "Black Vise");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
