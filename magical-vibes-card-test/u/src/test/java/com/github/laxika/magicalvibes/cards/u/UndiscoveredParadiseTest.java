package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.p.PoliticalTrickery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndiscoveredParadise.class, PoliticalTrickery.class})
class UndiscoveredParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds the chosen color and schedules the bounce")
    void manaAbilityAddsColorAndSchedulesBounce() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(land.isReturnToHandAtNextUntap()).isTrue();

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(before + 1);
        assertThat(land.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Returns to owner's hand during controller's next untap step")
    void returnsToHandAtNextUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        assertThat(land.isReturnToHandAtNextUntap()).isTrue();

        advanceTurn(); // player2's untap — still on battlefield
        harness.assertOnBattlefield(player1, "Undiscovered Paradise");

        advanceTurn(); // player1's untap — bounce

        harness.assertNotOnBattlefield(player1, "Undiscovered Paradise");
        harness.assertInHand(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Without activating it stays on the battlefield through untap")
    void staysWithoutActivation() {
        harness.addToBattlefield(player1, new UndiscoveredParadise());

        advanceTurn();
        advanceTurn();

        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Returns even when skip-untap would keep it tapped")
    void returnsEvenWhenItWouldNotUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        land.setSkipUntapCount(1);

        advanceTurn();
        advanceTurn();

        harness.assertInHand(player1, "Undiscovered Paradise");
        harness.assertNotOnBattlefield(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Does not bounce during an opponent's untap step")
    void doesNotBounceOnOpponentUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        advanceTurn(); // player2's untap

        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
        assertThat(land.isReturnToHandAtNextUntap()).isTrue();
    }

    @Test
    @DisplayName("Changing control does not move the scheduled return to the new controller's untap")
    void doesNotReturnDuringNewControllersUntap() {
        UndiscoveredParadise ownCard = new UndiscoveredParadise();
        ownCard.setOwnerId(player1.getId());
        Permanent own = harness.addToBattlefieldAndReturn(player1, ownCard);
        UndiscoveredParadise otherCard = new UndiscoveredParadise();
        otherCard.setOwnerId(player2.getId());
        Permanent other = harness.addToBattlefieldAndReturn(player2, otherCard);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.setHand(player1, List.of(new PoliticalTrickery()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), other.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own);
        advanceTurn();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own);
        harness.assertNotInHand(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Already untapped land still returns during the next untap step")
    void returnsEvenWhenAlreadyUntapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        land.untap();

        advanceTurn();
        advanceTurn();

        harness.assertNotOnBattlefield(player1, "Undiscovered Paradise");
        harness.assertInHand(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("A skipped untap step defers the return until the next actual untap step")
    void skippedUntapStepDefersReturn() {
        harness.addToBattlefield(player1, new UndiscoveredParadise());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        gd.skipNextUntapStepCount.put(player1.getId(), 1);

        advanceTurn();
        advanceTurn();
        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
        harness.assertNotInHand(player1, "Undiscovered Paradise");

        advanceTurn();
        advanceTurn();
        harness.assertNotOnBattlefield(player1, "Undiscovered Paradise");
        harness.assertInHand(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Land controlled by another player returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        UndiscoveredParadise card = new UndiscoveredParadise();
        card.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, card);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        advanceTurn();
        advanceTurn();

        harness.assertNotOnBattlefield(player1, "Undiscovered Paradise");
        harness.assertInHand(player2, "Undiscovered Paradise");
        harness.assertNotInHand(player1, "Undiscovered Paradise");
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
