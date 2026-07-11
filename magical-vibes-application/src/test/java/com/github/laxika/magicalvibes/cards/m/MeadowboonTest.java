package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeadowboonTest extends BaseCardTest {

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB puts +1/+1 on each creature the targeted controller controls")
    void evokeBuffsOwnCreatures() {
        Permanent ally1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent ally2 = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Meadowboon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities(); // resolve creature -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB (evoke sacrifice) -> LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, player1.getId()); // target self
        harness.passBothPriorities(); // resolve LTB trigger

        assertThat(ally1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Meadowboon itself was sacrificed as it entered.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Meadowboon"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Meadowboon"));
    }

    // ===== Leaves the battlefield (non-evoke) — target opponent =====

    @Test
    @DisplayName("LTB fires on any leave and can target an opponent: only that player's creatures get counters")
    void destroyedBuffsTargetedOpponentCreaturesOnly() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent oppCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent meadowboon = harness.addToBattlefieldAndReturn(player1, new Meadowboon());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, meadowboon);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, player2.getId()); // target opponent
        harness.passBothPriorities(); // resolve LTB trigger

        assertThat(oppCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }
}
