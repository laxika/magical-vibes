package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisTimeTwist.class, GrizzlyBears.class, Island.class})
class TeferisTimeTwistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature at the next end step with a +1/+1 counter")
    void returnsCreatureWithCounterAtNextEndStep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, originalId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a noncreature permanent without a +1/+1 counter")
    void returnsNoncreatureWithoutCounter() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalId = harness.getPermanentId(player1, "Island");
        harness.castInstant(player1, 0, originalId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Island");

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Island");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentsPermanent() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new TeferisTimeTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID opponentPermanentId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
