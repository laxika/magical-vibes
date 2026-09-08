package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarlockClass.class, GrizzlyBears.class, Shock.class})
class WarlockClassTest extends BaseCardTest {

    @Test
    void levelOneMakesEachOpponentLoseLifeIfACreatureDied() {
        harness.addToBattlefield(player1, new WarlockClass());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void levelOneDoesNothingIfNoCreatureDied() {
        harness.addToBattlefield(player1, new WarlockClass());
        harness.setLife(player2, 20);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void levelTwoPutsOneOfTheTopThreeCardsIntoHandAndTheRestIntoTheGraveyard() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        Card chosen = new GrizzlyBears();
        Card restOne = new Shock();
        Card restTwo = new Shock();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(warlockClass), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(warlockClass.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(restOne, restTwo);
    }

    @Test
    void levelThreeMakesEachOpponentLoseTheLifeTheyLostThisTurn() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        harness.setLibrary(player1, List.of());
        levelUpToThree(warlockClass);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void levelUpToThree(Permanent warlockClass) {
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, battlefieldIndex(warlockClass), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.activateAbility(player1, battlefieldIndex(warlockClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
