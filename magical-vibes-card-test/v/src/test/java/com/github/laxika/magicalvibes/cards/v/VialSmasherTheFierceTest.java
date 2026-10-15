package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VialSmasherTheFierce.class, GrizzlyBears.class, NicolBolasPlaneswalker.class,
        DarkRitual.class, WalkingBallista.class})
class VialSmasherTheFierceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the first spell's mana value to the opponent")
    void dealsDamageEqualToFirstSpellManaValue() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        castGrizzlyBears();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Only the first spell each turn triggers the ability")
    void onlyFirstSpellEachTurnTriggers() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        castGrizzlyBears();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        castGrizzlyBears();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can choose the randomly selected opponent's planeswalker")
    void canChooseOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castGrizzlyBears();

        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(planeswalker.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can damage the opponent even when they control a planeswalker")
    void canChooseOpponentInsteadOfPlaneswalker() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        harness.enterBattlefieldAndReturn(player1, new NicolBolasPlaneswalker());
        castGrizzlyBears();

        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(planeswalker.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Entering after the first spell does not make the second spell trigger")
    void doesNotTriggerIfFirstSpellWasCastBeforeEntering() {
        castGrizzlyBears();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new VialSmasherTheFierce());

        castGrizzlyBears();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's first spell does not trigger the ability")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The first spell on the opponent's turn triggers again")
    void firstSpellOnOpponentsTurnTriggersAgain() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        castGrizzlyBears();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("Both X symbols contribute to the triggering spell's mana value")
    void countsEachXSymbolInManaValue() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("A zero-mana-value first spell still consumes the turn's trigger")
    void zeroManaValueFirstSpellDoesNotAllowSecondSpellToTrigger() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        harness.setHand(player1, List.of(new WalkingBallista()));
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        castGrizzlyBears();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
