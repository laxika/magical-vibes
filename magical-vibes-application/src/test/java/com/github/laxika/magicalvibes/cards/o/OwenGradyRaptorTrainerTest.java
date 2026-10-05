package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BlueLoyalRaptor;
import com.github.laxika.magicalvibes.cards.e.EllieAndAlanPaleontologists;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OwenGradyRaptorTrainer.class, BlueLoyalRaptor.class, EllieAndAlanPaleontologists.class})
class OwenGradyRaptorTrainerTest extends BaseCardTest {

    @ParameterizedTest
    @CsvSource({
            "Put a reach counter on it, REACH, REACH",
            "Put a menace counter on it, MENACE, MENACE",
            "Put a trample counter on it, TRAMPLE, TRAMPLE",
            "Put a haste counter on it, HASTE, HASTE"
    })
    @DisplayName("Puts the chosen keyword counter on the target Dinosaur")
    void putsChosenCounterOnTargetDinosaur(String mode, CounterType counterType, Keyword keyword) {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(false);
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, dinosaur.getId());
        assertThat(trainer.isTapped()).isTrue();
        assertThat(dinosaur.getCounterCount(counterType)).isZero();
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);

        assertThat(dinosaur.getCounterCount(counterType)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dinosaur, keyword)).isTrue();
    }

    @Test
    @DisplayName("Can target only a Dinosaur")
    void cannotTargetNonDinosaur() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(false);
        Permanent nonDinosaur = harness.addToBattlefieldAndReturn(player1, new EllieAndAlanPaleontologists());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonDinosaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dinosaur");
    }

    @ParameterizedTest
    @CsvSource({"true, true", "true, false", "false, true", "false, false"})
    @DisplayName("Partner with lets either targeted player accept or decline the search for Blue")
    void partnerSearchIsOptionalForTargetedPlayer(boolean targetController, boolean accept) {
        var targetPlayer = targetController ? player1 : player2;
        Card blue = new BlueLoyalRaptor();
        harness.setLibrary(targetPlayer, List.of(blue));
        harness.castFromHand(player1, new OwenGradyRaptorTrainer(), "{1}{R}{G}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, targetPlayer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(targetPlayer.getId());
        harness.handleMayAbilityChosen(targetPlayer, accept);
        if (accept) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
            harness.handleCardChosen(targetPlayer, 0);
            assertThat(gd.playerHands.get(targetPlayer.getId())).contains(blue);
            assertThat(gd.playerDecks.get(targetPlayer.getId())).doesNotContain(blue);
        } else {
            assertThat(gd.playerDecks.get(targetPlayer.getId())).containsExactly(blue);
            assertThat(gd.playerHands.get(targetPlayer.getId())).doesNotContain(blue);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can put a counter on an opponent's Dinosaur")
    void canTargetOpposingDinosaur() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(false);
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player2, new BlueLoyalRaptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, dinosaur.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a haste counter on it");

        assertThat(dinosaur.getCounterCount(CounterType.HASTE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.HASTE)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"UPKEEP", "BEGINNING_OF_COMBAT", "END_STEP"})
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateOutsideMainPhase(TurnStep step) {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(false);
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trainer.isTapped()).isFalse();
        assertThat(dinosaur.getCounters()).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during an opponent's main phase")
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(false);
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trainer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while a spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(false);
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new EllieAndAlanPaleontologists(), "{2}{G}{W}{U}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trainer.isTapped()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true"})
    @DisplayName("The tap cost requires an untapped trainer without summoning sickness")
    void cannotActivateWhenUnableToPayTapCost(boolean summoningSick, boolean tapped) {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        trainer.setSummoningSick(summoningSick);
        trainer.setTapped(tapped);
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dinosaur.getCounters()).isEmpty();
    }
}
