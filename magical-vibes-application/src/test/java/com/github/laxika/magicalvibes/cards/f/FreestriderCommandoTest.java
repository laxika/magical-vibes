package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvenInterrupter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreestriderCommando.class, AvenInterrupter.class})
class FreestriderCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters when it was not cast")
    void entersWithCountersWhenPutOntoBattlefield() {
        Permanent commando = harness.addToBattlefieldAndReturn(player1, new FreestriderCommando());

        assertThat(commando.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters without counters when cast by paying mana")
    void entersWithoutCountersWhenCastNormally() {
        FreestriderCommando commando = new FreestriderCommando();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(commando));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Freestrider Commando")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with counters when cast from plot without paying mana")
    void entersWithCountersWhenCastFromPlot() {
        FreestriderCommando commando = plotCommando(player1);

        advanceToNextMainPhase(player1);
        harness.castFromExile(player1, commando.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Freestrider Commando")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get counters when an additional cost makes a plotted cast spend mana")
    void doesNotEnterWithCountersWhenPlotCastPaysAdditionalMana() {
        harness.addToBattlefield(player1, new AvenInterrupter());
        FreestriderCommando commando = plotCommando(player2);

        advanceToNextMainPhase(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, commando.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Freestrider Commando")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private FreestriderCommando plotCommando(Player player) {
        FreestriderCommando commando = new FreestriderCommando();
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(commando));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player, 0, List.of());
        return commando;
    }

    private void advanceToNextMainPhase(Player player) {
        Player otherPlayer = player.equals(player1) ? player2 : player1;
        harness.passUntil(otherPlayer, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
