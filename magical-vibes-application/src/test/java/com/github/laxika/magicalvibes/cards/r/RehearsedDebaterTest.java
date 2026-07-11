package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RehearsedDebaterTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting an instant that targets a creature gives +1/+1 until end of turn")
    void reparteeBoostsSelf() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Rehearsed Debater"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    @DisplayName("Repartee boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(3);
    }
}
