package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.PersistentNightmare;
import com.github.laxika.magicalvibes.cards.s.StartledAwake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorruptionOfTowashi.class, StartledAwake.class, PersistentNightmare.class})
class CorruptionOfTowashiTest extends BaseCardTest {

    @Test
    void entersWithAnIncubatorTokenWithFourCounters() {
        castCorruption();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
    }

    @Test
    void drawsWhenAnIncubatorTransforms() {
        castCorruption();
        harness.setLibrary(player1, List.of(new StartledAwake()));

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(incubator.isTransformed()).isTrue();
    }

    @Test
    void onlyDrawsOnceWhenASeparatePermanentEntersTransformedThisTurn() {
        castCorruption();
        harness.setLibrary(player1, List.of(new StartledAwake(), new StartledAwake()));

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Persistent Nightmare").isTransformed()).isTrue();
    }

    @Test
    void decliningADrawAllowsDrawingForALaterTransformation() {
        castCorruption();
        harness.setLibrary(player1, List.of(new CorruptionOfTowashi()));
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsWhenAPermanentEntersTransformedWithoutAnEarlierTransformation() {
        castCorruption();
        harness.setLibrary(player1, List.of(new CorruptionOfTowashi()));
        harness.setGraveyard(player1, List.of(new StartledAwake()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Persistent Nightmare").isTransformed()).isTrue();
    }

    @Test
    void anOpponentsTransformedPermanentDoesNotOfferADraw() {
        castCorruption();
        harness.setLibrary(player1, List.of(new CorruptionOfTowashi()));
        harness.setGraveyard(player2, List.of(new StartledAwake()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.activateGraveyardAbility(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player2, "Persistent Nightmare").isTransformed()).isTrue();
    }

    private void castCorruption() {
        harness.castFromHand(player1, new CorruptionOfTowashi(), "{4}{U}");
        resolveAllTriggers();
    }
}
