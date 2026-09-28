package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElegantParlor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HedronCrab;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MirkoObsessiveTheorist.class, ElegantParlor.class, HedronCrab.class,
        Memnite.class, GrizzlyBears.class})
class MirkoObsessiveTheoristTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you surveil, Mirko gets a +1/+1 counter")
    void surveilingPutsCounterOnMirko() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ElegantParlor()));
        harness.playLand(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(mirko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("End step returns only a creature card with power less than Mirko and gives it a finality counter")
    void endStepReturnsStrictlyLowerPowerCreatureWithFinalityCounter() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        Card lowerPower = new HedronCrab();
        Card equalPower = new Memnite();
        Card greaterPower = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(lowerPower, equalPower, greaterPower));

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(lowerPower.getId());

        harness.handleMultipleCardsChosen(player1, List.of(lowerPower.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(lowerPower.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(equalPower, greaterPower);
        assertThat(mirko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The optional end-step return can be declined")
    void endStepReturnCanBeDeclined() {
        harness.addToBattlefield(player1, new MirkoObsessiveTheorist());
        Card lowerPower = new HedronCrab();
        harness.setGraveyard(player1, List.of(lowerPower));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lowerPower);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(lowerPower.getId()));
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
