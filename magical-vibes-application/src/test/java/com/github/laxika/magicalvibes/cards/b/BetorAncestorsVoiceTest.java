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
        resolveAllTriggers();

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

    @Test
    @DisplayName("Counter placement and reanimation resolve as one ability")
    void countersAndReanimationResolveTogether() {
        addCreatureReady(player1, new BetorAncestorsVoice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears graveyardTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardTarget));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardTarget.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("Life gained after triggering is included when counters resolve")
    void countsLifeGainedBeforeResolution() {
        addCreatureReady(player1, new BetorAncestorsVoice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        addCreatureReady(player1, new BetorAncestorsVoice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("May decline returning an eligible creature")
    void mayDeclineReanimation() {
        harness.addToBattlefield(player1, new BetorAncestorsVoice());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
