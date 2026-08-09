package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MossdogTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when targeted by an opponent's spell")
    void getsCounterWhenTargetedByOpponentSpell() {
        harness.addToBattlefield(player1, new Mossdog());
        UUID mossdogId = harness.getPermanentId(player1, "Mossdog");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, mossdogId);
        harness.passBothPriorities();

        Permanent mossdog = findPermanent(player1, "Mossdog");
        assertThat(mossdog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mossdog.getEffectivePower()).isEqualTo(2);
        assertThat(mossdog.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when targeted by an opponent's ability")
    void getsCounterWhenTargetedByOpponentAbility() {
        harness.addToBattlefield(player1, new Mossdog());
        UUID mossdogId = harness.getPermanentId(player1, "Mossdog");

        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, mossdogId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mossdog")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when its controller's own spell targets it")
    void doesNotTriggerOnOwnSpell() {
        harness.addToBattlefield(player1, new Mossdog());
        UUID mossdogId = harness.getPermanentId(player1, "Mossdog");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mossdogId);

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanent(player1, "Mossdog")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
