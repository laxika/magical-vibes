package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VannifarEvolvedEnigma.class, GrizzlyBears.class, MyrSire.class})
class VannifarEvolvedEnigmaTest extends BaseCardTest {

    @Test
    void cloaksACardFromHand() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        Card card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Cloak a card from your hand");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .findFirst()
                .orElseThrow();
        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(gqs.isCreature(gd, cloaked)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void putsCountersOnColorlessCreaturesOnly() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        Permanent colorlessCreature = harness.addToBattlefieldAndReturn(player1, new MyrSire());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Put a +1/+1 counter on each colorless creature you control");
        harness.passBothPriorities();

        assertThat(colorlessCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(coloredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
