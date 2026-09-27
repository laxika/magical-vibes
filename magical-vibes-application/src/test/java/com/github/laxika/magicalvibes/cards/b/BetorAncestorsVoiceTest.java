package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BetorAncestorsVoice.class, GrizzlyBears.class, ThunderingGiant.class})
class BetorAncestorsVoiceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters equal to life gained on another creature you control")
    void putsCountersEqualToLifeGainedOnAnotherCreatureYouControl() {
        Permanent betor = addCreatureReady(player1, new BetorAncestorsVoice());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentTarget = addCreatureReady(player2, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(ownTarget.getId());

        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponentTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(betor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Returns a creature with mana value at most life lost")
    void returnsCreatureWithManaValueAtMostLifeLost() {
        Permanent betor = addCreatureReady(player1, new BetorAncestorsVoice());
        GrizzlyBears eligible = new GrizzlyBears();
        ThunderingGiant ineligible = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(eligible, ineligible));
        gd.lifeLostThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Thundering Giant");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(betor.getCard().getId()));
    }

    @Test
    @DisplayName("Does not target a creature whose mana value exceeds life lost")
    void doesNotTargetCreatureAboveLifeLost() {
        harness.addToBattlefield(player1, new BetorAncestorsVoice());
        harness.setGraveyard(player1, List.of(new ThunderingGiant()));
        gd.lifeLostThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Thundering Giant");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
