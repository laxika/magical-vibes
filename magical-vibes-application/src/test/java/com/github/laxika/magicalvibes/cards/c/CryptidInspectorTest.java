package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AbzanGuide;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryptidInspector.class, AbzanGuide.class, GrizzlyBears.class})
class CryptidInspectorTest extends BaseCardTest {

    @Test
    void putsCounterOnFaceDownPermanentEnteringUnderYourControl() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new CryptidInspector());
        harness.setHand(player1, List.of(new AbzanGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterWhenControlledPermanentTurnsFaceUp() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new CryptidInspector());
        harness.setHand(player1, List.of(new AbzanGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent guide = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isFaceDown())
                .findFirst()
                .orElseThrow();
        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guide));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForFaceUpOrOpponentControlledPermanents() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new CryptidInspector());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AbzanGuide()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();

        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
