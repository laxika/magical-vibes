package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NovijenSages.class, MistralCharger.class})
class NovijenSagesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourCounters() {
        Permanent sages = castSages();

        assertThat(sages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Graft moves a +1/+1 counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent sages = castSages();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(sages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may decline moving a counter onto an entering creature")
    void graftMayBeDeclined() {
        Permanent sages = castSages();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(sages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft may move a counter onto a creature entering under an opponent's control")
    void graftMovesCounterOntoOpponentsEnteringCreature() {
        Permanent sages = castSages();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player2, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(sages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability removes two +1/+1 counters from among creatures and draws a card")
    void removesTwoCountersAndDraws() {
        Permanent sages = addCreatureReady(player1, new NovijenSages());
        sages.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(sages.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot use +1/+1 counters on an opponent's creature")
    void abilityRequiresCountersOnControlledCreatures() {
        Permanent sages = addCreatureReady(player1, new NovijenSages());
        sages.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponentCharger = addCreatureReady(player2, new MistralCharger());
        opponentCharger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(sages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot be activated without two +1/+1 counters among creatures")
    void abilityRequiresTwoCounters() {
        Permanent sages = addCreatureReady(player1, new NovijenSages());
        sages.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sages can pay both counters from itself")
    void tappedSummoningSickSagesCanDraw() {
        Permanent sages = castSages();
        sages.tap();
        harness.setLibrary(player1, List.of(new MistralCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(sages.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draw resolves after removing Sages' last two counters kills it")
    void drawResolvesAfterSourceDiesToCost() {
        Permanent sages = castSages();
        sages.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new MistralCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Novijen Sages");
        harness.assertInGraveyard(player1, "Novijen Sages");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Sages can draw using counters exclusively from another creature")
    void removesBothCountersFromAnotherCreature() {
        Permanent sages = castSages();
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new MistralCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, charger.getId());
        harness.handlePermanentChosen(player1, charger.getId());
        harness.passBothPriorities();

        assertThat(sages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private Permanent castSages() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new NovijenSages(), "{4}{U}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Novijen Sages");
    }
}
