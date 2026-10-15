package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LionVulture.class, Forest.class, Shock.class})
class LionVultureTest extends BaseCardTest {

    @Test
    void putsCounterOnItselfAndDrawsAtEndStepAfterOpponentLosesLife() {
        Permanent lionVulture = harness.addToBattlefieldAndReturn(player1, new LionVulture());
        harness.setLibrary(player1, List.of(new Forest()));
        dealDamageToOpponent();

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(lionVulture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Object::getClass)
                .contains(Forest.class);
    }

    @Test
    void doesNothingAtEndStepWhenNoOpponentLostLife() {
        Permanent lionVulture = harness.addToBattlefieldAndReturn(player1, new LionVulture());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep();

        assertThat(lionVulture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Object::getClass)
                .doesNotContain(Forest.class);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Object::getClass)
                .containsExactly(Forest.class);
    }

    @Test
    void triggersForLifeLostBeforeItEnteredTheBattlefield() {
        dealDamageToOpponent();
        Permanent lionVulture = harness.addToBattlefieldAndReturn(player1, new LionVulture());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(lionVulture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent lionVulture = harness.addToBattlefieldAndReturn(player1, new LionVulture());
        harness.setLibrary(player1, List.of(new Forest()));
        dealDamageToOpponent();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(lionVulture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWhenOnlyItsControllerLostLife() {
        Permanent lionVulture = harness.addToBattlefieldAndReturn(player1, new LionVulture());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(lionVulture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void stillDrawsWhenItDiesInResponseToItsTrigger() {
        Permanent lionVulture = harness.addToBattlefieldAndReturn(player1, new LionVulture());
        harness.setLibrary(player1, List.of(new Forest()));
        dealDamageToOpponent();
        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, lionVulture.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lionVulture);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Object::getClass)
                .containsExactly(Forest.class);
    }

    private void dealDamageToOpponent() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
