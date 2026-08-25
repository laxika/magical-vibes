package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StreetwiseNegotiator.class, GoblinPiker.class})
class StreetwiseNegotiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Streetwise Negotiator assigns combat damage equal to its toughness")
    void assignsCombatDamageUsingItsToughness() {
        Permanent negotiator = harness.addToBattlefieldAndReturn(player1, new StreetwiseNegotiator());
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, negotiator)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Backup grants another creature toughness-based combat damage until end of turn")
    void backupGrantsToughnessBasedCombatDamage() {
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        castStreetwiseNegotiatorTargeting(piker);

        assertThat(piker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(3);
    }

    private void castStreetwiseNegotiatorTargeting(Permanent target) {
        harness.setHand(player1, List.of(new StreetwiseNegotiator()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
