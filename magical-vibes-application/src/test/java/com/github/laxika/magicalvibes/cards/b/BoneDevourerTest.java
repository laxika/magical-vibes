package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneDevourer.class, GrizzlyBears.class, Shock.class})
class BoneDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter after a creature dies this turn")
    void entersWithCountersForCreaturesThatDiedThisTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock(), new BoneDevourer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent boneDevourer = findPermanent(player1, "Bone Devourer");
        assertThat(boneDevourer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("On death, draws cards and loses life equal to its +1/+1 counters")
    void onDeathDrawsAndLosesLifePerCounter() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent boneDevourer = addCreatureReady(player1, new BoneDevourer());
        boneDevourer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, boneDevourer));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
