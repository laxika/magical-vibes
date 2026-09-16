package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IshaiOjutaiDragonspeaker.class, GrizzlyBears.class, Ornithopter.class})
class IshaiOjutaiDragonspeakerTest extends BaseCardTest {

    @Test
    void opponentCastingASpellAddsACounter() {
        harness.addToBattlefield(player1, new IshaiOjutaiDragonspeaker());
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        Permanent ishai = getIshai();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(ishai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void controllerCastingASpellDoesNotAddACounter() {
        harness.addToBattlefield(player1, new IshaiOjutaiDragonspeaker());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent ishai = getIshai();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(ishai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentCastingAColorlessSpellAddsACounter() {
        harness.addToBattlefield(player1, new IshaiOjutaiDragonspeaker());
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new Ornithopter()));

        Permanent ishai = getIshai();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(ishai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent getIshai() {
        return findPermanent(player1, "Ishai, Ojutai Dragonspeaker");
    }
}
