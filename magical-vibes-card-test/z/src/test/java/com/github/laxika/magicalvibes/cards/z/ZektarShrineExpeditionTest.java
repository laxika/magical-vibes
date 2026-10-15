package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Zektar Shrine Expedition")
@CardUsed({ZektarShrineExpedition.class, Forest.class})
class ZektarShrineExpeditionTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers a quest counter")
    void landfallOffersQuestCounter() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining landfall adds no quest counter")
    void decliningLandfallAddsNoQuestCounter() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing three quest counters and sacrificing creates a hasty trampling Elemental")
    void removesCountersSacrificesAndCreatesElemental() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(expedition);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
        assertThat(elemental.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The Elemental is exiled at the beginning of the next end step")
    void elementalIsExiledAtNextEndStep() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("The ability cannot be activated without three quest counters")
    void cannotActivateWithoutThreeQuestCounters() {
        addExpedition();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landEnteringWithoutBeingPlayedTriggersLandfall() {
        Permanent expedition = addExpedition();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent expedition = addExpedition();

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void twoQuestCountersAndAnotherCounterCannotPayCost() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 2);
        expedition.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(expedition);
    }

    @Test
    void activationPaysCostsBeforeCreatingToken() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(expedition);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expedition.getCard());
        assertThat(countPermanents(player1, "Elemental")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    void exileUsesTheStackAtTheNextEndStep() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    void tokenCreatedDuringEndStepWaitsUntilFollowingEndStep() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, null, null);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    void newlyEnteredExpeditionCanActivateDuringOpponentsTurn() {
        Permanent expedition = harness.addToBattlefieldAndReturn(player1, new ZektarShrineExpedition());
        expedition.setCounterCount(CounterType.QUEST, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elemental")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expedition.getCard());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    void eachExpeditionGetsItsOwnLandfallCounter() {
        Permanent first = addExpedition();
        Permanent second = addExpedition();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(first.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    private Permanent addExpedition() {
        return harness.addToBattlefieldAndReturn(player1, new ZektarShrineExpedition());
    }
}
