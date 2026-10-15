package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CinderhazeWretch.class, Cinderbones.class, Forest.class})
class CinderhazeWretchTest extends BaseCardTest {

    // ===== Discard ability =====

    @Test
    @DisplayName("Tap ability makes the target player discard a card")
    void tapAbilityDiscards() {
        addCreatureReady(player1, new CinderhazeWretch());
        harness.setHand(player2, List.of(new Cinderbones(), new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tap ability can target its controller")
    void tapAbilityCanTargetController() {
        addCreatureReady(player1, new CinderhazeWretch());
        harness.setHand(player1, List.of(new Cinderbones()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tap ability cannot be activated during opponent's turn")
    void tapAbilityOnlyDuringYourTurn() {
        addCreatureReady(player1, new CinderhazeWretch());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    // ===== Untap ability =====

    @Test
    @DisplayName("Untap ability untaps the Wretch and puts a -1/-1 counter on it as a cost")
    void untapAbilityUntapsAndAddsCounter() {
        Permanent wretch = addCreatureReady(player1, new CinderhazeWretch());
        wretch.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        // Cost is paid immediately on activation.
        assertThat(wretch.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(wretch.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability can be activated during an opponent's turn")
    void untapAbilityCanBeActivatedDuringOpponentsTurn() {
        Permanent wretch = addCreatureReady(player1, new CinderhazeWretch());
        wretch.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(wretch.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(wretch.isTapped()).isFalse();
    }
    @Test
    @DisplayName("Discard ability resolves harmlessly when the target has an empty hand")
    void discardFromEmptyHand() {
        Permanent wretch = addCreatureReady(player1, new CinderhazeWretch());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(wretch.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents the tap ability")
    void summoningSicknessPreventsDiscard() {
        harness.addToBattlefield(player1, new CinderhazeWretch());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Summoning sickness does not prevent the counter-cost untap ability")
    void summoningSicknessDoesNotPreventUntap() {
        Permanent wretch = harness.addToBattlefieldAndReturn(player1, new CinderhazeWretch());
        wretch.tap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(wretch.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(wretch.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(wretch.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A lethal counter cost is legal and the creature dies before untapping")
    void lethalCounterCostKillsBeforeUntap() {
        Permanent wretch = addCreatureReady(player1, new CinderhazeWretch());
        wretch.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        wretch.tap();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Cinderhaze Wretch");
        harness.assertInGraveyard(player1, "Cinderhaze Wretch");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
