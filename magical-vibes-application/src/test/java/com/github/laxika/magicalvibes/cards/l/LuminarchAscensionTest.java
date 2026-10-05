package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Souldrinker;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuminarchAscension.class, Shock.class, Souldrinker.class, IntoTheRoil.class})
class LuminarchAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's end step offers a quest counter when the controller lost no life")
    void opponentEndStepOffersQuestCounter() {
        Permanent ascension = addAscension();

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the opponent end-step trigger adds no quest counter")
    void decliningQuestCounterTriggerAddsNothing() {
        Permanent ascension = addAscension();

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("The trigger does not occur after the controller loses life this turn")
    void lifeLossPreventsQuestCounterTrigger() {
        Permanent ascension = addAscension();
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Paying life also prevents the trigger")
    void lifePaymentPreventsQuestCounterTrigger() {
        Permanent ascension = addAscension();
        Permanent souldrinker = harness.addToBattlefieldAndReturn(player1, new Souldrinker());
        souldrinker.setSummoningSick(false);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("The trigger does not occur during the controller's own end step")
    void ownEndStepDoesNotTrigger() {
        Permanent ascension = addAscension();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("The Angel ability requires four quest counters")
    void angelAbilityRequiresFourQuestCounters() {
        Permanent ascension = addAscension();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quest counters");

        ascension.setCounterCount(CounterType.QUEST, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(angel.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(angel.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Life lost before Ascension enters the battlefield still prevents its trigger")
    void lifeLostBeforeEnteringBattlefieldPreventsTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
        Permanent ascension = addAscension();

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("An opponent losing life does not prevent the controller's quest counter")
    void opponentLifeLossDoesNotPreventTrigger() {
        Permanent ascension = addAscension();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.assertLife(player2, 18);

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Life lost in response prevents the quest counter at resolution")
    void lifeLostInResponsePreventsQuestCounter() {
        Permanent ascension = addAscension();
        harness.addToBattlefield(player1, new Souldrinker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Three quest counters do not permit creating an Angel")
    void threeQuestCountersAreInsufficient() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quest counters");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Creating Angels is repeatable and does not consume quest counters")
    void angelAbilityCanBeRepeatedWithoutConsumingCounters() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 5);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(5);
    }

    @Test
    @DisplayName("The fourth quest counter enables activation during the opponent's end step")
    void fourthQuestCounterEnablesImmediateActivation() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
    }

    @Test
    @DisplayName("An activated Angel ability resolves even after Ascension leaves the battlefield")
    void angelAbilityResolvesAfterSourceLeavesBattlefield() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveInstant(player2, 0, ascension.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().isToken()).isTrue();
    }

    private Permanent addAscension() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new LuminarchAscension());
        ascension.setSummoningSick(false);
        return ascension;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        if (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
