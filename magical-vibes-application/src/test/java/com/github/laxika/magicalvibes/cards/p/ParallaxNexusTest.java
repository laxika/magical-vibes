package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParallaxNexus.class, SpinelessThug.class})
class ParallaxNexusTest extends BaseCardTest {

    @Test
    void entersWithFiveFadeCounters() {
        Permanent nexus = castAndResolveNexus();

        assertThat(nexus.getCounterCount(CounterType.FADE)).isEqualTo(5);
    }

    @Test
    void removesFadeCounterAtUpkeep() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(nexus.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nexus);
    }

    @Test
    void sacrificesWithoutFadeCounters() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nexus);
    }

    @Test
    void doesNotRemoveFadeCounterOnOpponentsUpkeep() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(nexus.getCounterCount(CounterType.FADE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nexus);
    }

    @Test
    void targetOpponentChoosesCardAndItReturnsWhenNexusLeaves() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 1);
        SpinelessThug exiledCard = new SpinelessThug();
        SpinelessThug retainedCard = new SpinelessThug();
        harness.setHand(player2, List.of(exiledCard, retainedCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retainedCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledCard);
        assertThat(gd.getCardsExiledByPermanent(nexus.getId())).contains(exiledCard);
        assertThat(nexus.getCounterCount(CounterType.FADE)).isZero();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nexus);
        assertThat(gd.playerHands.get(player2.getId())).contains(retainedCard, exiledCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(exiledCard);
    }

    @Test
    void resolvesWithoutPromptWhenTargetOpponentHasNoCards() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 1);
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(nexus.getCounterCount(CounterType.FADE)).isZero();
        assertThat(gd.getCardsExiledByPermanent(nexus.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithoutFadeCounter() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetController() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateOnOpponentsTurn() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castAndResolveNexus() {
        ParallaxNexus card = new ParallaxNexus();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, card, "{2}{B}");
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
