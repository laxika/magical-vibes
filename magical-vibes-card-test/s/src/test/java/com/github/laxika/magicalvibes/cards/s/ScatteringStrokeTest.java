package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScatteringStroke.class, Forest.class, DeeptreadMerrow.class, WingsOfVelisVel.class})
class ScatteringStrokeTest extends BaseCardTest {

    // Player1 casts Deeptread Merrow (mana value 2); Player2 counters it with Scattering Stroke.
    private DeeptreadMerrow prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        DeeptreadMerrow merrow = new DeeptreadMerrow();
        harness.setHand(player1, List.of(merrow));
        harness.addMana(player1, ManaColor.BLUE, 2); // {1}{U}

        harness.setHand(player2, List.of(new ScatteringStroke()));
        harness.addMana(player2, ManaColor.BLUE, 4); // {2}{U}{U}

        return merrow;
    }

    // Player2 (caster) wins the clash: their revealed top card has a strictly greater mana value.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new DeeptreadMerrow(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash counters the spell and lets the caster add {C} equal to its mana value next main phase")
    void wonClashSchedulesManaEqualToCounteredSpellManaValue() {
        DeeptreadMerrow merrow = prepareCounterTarget();
        stackClashWinForCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, merrow.getId());
        keepClashCardsOnTop();

        // Spell was countered.
        harness.assertNotOnBattlefield(player1, "Deeptread Merrow");
        harness.assertInGraveyard(player1, "Deeptread Merrow");

        // Delayed reward registered for the caster, amount = Deeptread Merrow's mana value (2).
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
        AddManaAtNextMainPhase reward = gd.getDelayedActions(AddManaAtNextMainPhase.class).getFirst();
        assertThat(reward.controllerId()).isEqualTo(player2.getId());
        assertThat(reward.amount()).isEqualTo(2);
        assertThat(reward.color()).isEqualTo(ManaColor.COLORLESS);

        // At the caster's next main phase, they may add that much {C}.
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();

        harness.passBothPriorities(); // resolve the delayed "you may" trigger
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the delayed reward adds no mana")
    void wonClashDelayedRewardMayBeDeclined() {
        DeeptreadMerrow merrow = prepareCounterTarget();
        stackClashWinForCaster();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, merrow.getId());
        keepClashCardsOnTop();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Losing the clash counters the spell but schedules no mana")
    void lostClashCountersButSchedulesNoMana() {
        DeeptreadMerrow merrow = prepareCounterTarget();
        // Player2 loses the clash: player1 reveals the higher mana value.
        harness.setLibrary(player1, List.of(
                new DeeptreadMerrow(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, merrow.getId());
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Deeptread Merrow");
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();
    }

    @Test
    @DisplayName("A tied clash counters the spell but schedules no mana")
    void tiedClashCountersButSchedulesNoMana() {
        DeeptreadMerrow merrow = prepareCounterTarget();
        harness.setLibrary(player1, List.of(
                new DeeptreadMerrow(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(
                new DeeptreadMerrow(), new Forest(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, merrow.getId());
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Deeptread Merrow");
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();
    }

    @Test
    @DisplayName("Winning the clash also rewards the caster at a postcombat next main phase")
    void wonClashSchedulesManaAtPostcombatNextMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var creature = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());
        WingsOfVelisVel wings = new WingsOfVelisVel();
        harness.setHand(player1, List.of(wings));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new ScatteringStroke()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        stackClashWinForCaster();

        harness.passPriority(player2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, wings.getId());
        keepClashCardsOnTop();
        harness.assertInGraveyard(player1, "Wings of Velis Vel");

        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    private void keepClashCardsOnTop() {
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("The target spell is countered before clash placement choices")
    void countersBeforeClashPlacement() {
        DeeptreadMerrow merrow = prepareCounterTarget();
        stackClashWinForCaster();
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, merrow.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.assertInGraveyard(player1, "Deeptread Merrow");
        assertThat(gd.stack).noneMatch(entry -> entry.getTargetableId().equals(merrow.getId()));
    }

    @Test
    @DisplayName("Clash placement can put both revealed cards on the bottom")
    void bothPlayersCanBottomTheirRevealedCards() {
        DeeptreadMerrow merrow = prepareCounterTarget();
        Forest opponentTop = new Forest();
        DeeptreadMerrow casterTop = new DeeptreadMerrow();
        Forest opponentNext = new Forest();
        Forest casterNext = new Forest();
        harness.setLibrary(player1, List.of(opponentTop, opponentNext));
        harness.setLibrary(player2, List.of(casterTop, casterNext));
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, merrow.getId());
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentNext, opponentTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(casterNext, casterTop);
        harness.assertInGraveyard(player1, "Deeptread Merrow");
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
    }
}
