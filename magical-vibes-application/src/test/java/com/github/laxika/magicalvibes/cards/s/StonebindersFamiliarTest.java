package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JourneyToNowhere;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StonebindersFamiliarTest extends BaseCardTest {

    @Test
    void getsACounterWhenACardIsExiledDuringYourTurn() {
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent familiar = findPermanent(player1, "Stonebinder's Familiar");

        exileWithJourney(harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersOnlyOncePerTurn() {
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent familiar = findPermanent(player1, "Stonebinder's Familiar");

        exileWithJourney(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.addToBattlefield(player2, new GrizzlyBears());
        exileWithJourney(harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringAnOpponentsTurn() {
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent familiar = findPermanent(player1, "Stonebinder's Familiar");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new JourneyToNowhere()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castEnchantment(player2, 0,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void exileWithJourney(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JourneyToNowhere()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
