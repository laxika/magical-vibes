package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AttunedHunterTest extends BaseCardTest {

    @Test
    void putsOneCounterWhenCardsLeaveGraveyardDuringYourTurn() {
        Permanent hunter = addReadyHunter(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));

        castReminisce(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentTurn() {
        Permanent hunter = addReadyHunter(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new Shock()));

        castReminisce(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyHunter(Player player) {
        Permanent hunter = harness.addToBattlefieldAndReturn(player, new AttunedHunter());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return hunter;
    }

    private void castReminisce(Player caster, UUID targetPlayerId) {
        harness.setHand(caster, List.of(new Reminisce()));
        harness.addMana(caster, ManaColor.BLUE, 3);
        harness.castSorcery(caster, 0, targetPlayerId);
    }
}
