package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void targetOpponentChoosesCardAndItReturnsWhenNexusLeaves() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        nexus.setCounterCount(CounterType.FADE, 1);
        Card exiledCard = new GrizzlyBears();
        harness.setHand(player2, List.of(exiledCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(exiledCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledCard);
        assertThat(gd.getCardsExiledByPermanent(nexus.getId())).contains(exiledCard);
        assertThat(nexus.getCounterCount(CounterType.FADE)).isZero();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nexus);
        assertThat(gd.playerHands.get(player2.getId())).contains(exiledCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(exiledCard);
    }

    @Test
    void cannotTargetController() {
        harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateOutsideSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new ParallaxNexus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castAndResolveNexus() {
        ParallaxNexus card = new ParallaxNexus();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
