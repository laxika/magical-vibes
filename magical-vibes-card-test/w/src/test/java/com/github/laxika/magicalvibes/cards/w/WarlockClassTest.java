package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarlockClass.class, GrizzlyBears.class, Shock.class})
class WarlockClassTest extends BaseCardTest {

    @Test
    void levelOneMakesEachOpponentLoseLifeIfACreatureDied() {
        harness.addToBattlefield(player1, new WarlockClass());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
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

        assertThat(warlockClass.getClassLevel()).isEqualTo(2);
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

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void creatureDyingBeforeClassEntersStillEnablesLevelOne() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.addToBattlefield(player1, new WarlockClass());

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void creatureDyingDuringEndStepDoesNotEnableLevelOneRetroactively() {
        harness.addToBattlefield(player1, new WarlockClass());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        resolveEndStep(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void levelTwoWorksWithFewerThanThreeCardsInLibrary() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        Card chosen = new GrizzlyBears();
        Card remaining = new Shock();
        harness.setLibrary(player1, List.of(chosen, remaining));
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, battlefieldIndex(warlockClass), 0, null, null);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void levelThreeDoesNothingIfOpponentHasLostNoLife() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        harness.setLibrary(player1, List.of());
        levelUpToThree(warlockClass);
        harness.setLife(player2, 20);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(warlockClass.getClassLevel()).isEqualTo(3);
    }

    @Test
    void levelThreeDoesNotTriggerDuringOpponentsEndStep() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        harness.setLibrary(player1, List.of());
        levelUpToThree(warlockClass);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        resolveEndStep(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void cannotSkipLevelTwo() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(warlockClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(warlockClass.getClassLevel()).isEqualTo(1);
    }

    @Test
    void cannotLevelUpDuringEndStep() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(warlockClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(warlockClass.getClassLevel()).isEqualTo(1);
    }

    @Test
    void levelThreeCountsLifeLostInResponseToItsTrigger() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        harness.setLibrary(player1, List.of());
        levelUpToThree(warlockClass);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void levelThreeRetainsLevelOneAbility() {
        Permanent warlockClass = harness.addToBattlefieldAndReturn(player1, new WarlockClass());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        levelUpToThree(warlockClass);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isBetween(18, 19);
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
