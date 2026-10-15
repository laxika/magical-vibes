package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TiminYouthfulGeist.class, GrizzlyBears.class})
class TiminYouthfulGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Rhoda")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card rhoda = namedCard("Rhoda, Geist Avenger");
        harness.setLibrary(player2, List.of(rhoda));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new TiminYouthfulGeist());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(rhoda);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("At the beginning of each combat, Timin taps up to one target creature")
    void tapsTargetCreatureAtBeginningOfCombat() {
        harness.addToBattlefieldAndReturn(player1, new TiminYouthfulGeist());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Timin's combat trigger can choose no creature")
    void combatTriggerCanChooseNoCreature() {
        harness.addToBattlefieldAndReturn(player1, new TiminYouthfulGeist());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
