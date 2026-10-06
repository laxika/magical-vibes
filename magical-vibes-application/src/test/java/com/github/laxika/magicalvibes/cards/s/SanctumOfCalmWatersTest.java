package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarruksGorehorn;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumOfCalmWaters.class, SanctumOfShatteredHeights.class, GarruksGorehorn.class, Forest.class})
class SanctumOfCalmWatersTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger draws for each Shrine, then discards a card")
    void acceptingTriggerDrawsForEachShrineThenDiscards() {
        harness.setHand(player1, new ArrayList<>(List.of(new GarruksGorehorn())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "Garruk's Gorehorn");
    }

    @Test
    @DisplayName("Declining the trigger does not draw or discard")
    void decliningTriggerDoesNothing() {
        harness.setHand(player1, List.of(new GarruksGorehorn()));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger does not happen on an opponent's first main phase")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only controlled Shrines count, including the source itself")
    void countsOnlyControlledShrines() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        harness.addToBattlefield(player1, new GarruksGorehorn());
        harness.addToBattlefield(player2, new SanctumOfShatteredHeights());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Shrines entering after the trigger are counted on resolution")
    void countsShrinesAtResolution() {
        harness.setHand(player1, List.of(new GarruksGorehorn()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());

        advanceToPrecombatMain(player1);
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Removing the source does not remove its trigger or count its former presence")
    void sourceRemovalStillResolvesWithCurrentShrineCount() {
        harness.setHand(player1, List.of(new GarruksGorehorn()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        var source = harness.addToBattlefieldAndReturn(player1, new SanctumOfCalmWaters());
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());

        advanceToPrecombatMain(player1);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setGraveyard(player1, List.of(source.getCard()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Garruk's Gorehorn");
    }

    @Test
    @DisplayName("The ability does not trigger at the beginning of the second main phase")
    void doesNotTriggerInSecondMainPhase() {
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
