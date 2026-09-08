package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiremindsResearchTest extends BaseCardTest {

    @Test
    void putsAChargeCounterOnItWhenYouCastAnInstantOrSorcery() {
        Permanent research = addResearch();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(research.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForCreatureSpells() {
        Permanent research = addResearch();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(research.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void removesTwoChargeCountersAndDrawsACard() {
        Permanent research = addResearch();
        research.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(research.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void removesFiveChargeCountersAndDealsFiveDamageToAnyTarget() {
        Permanent research = addResearch();
        research.setCounterCount(CounterType.CHARGE, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(research.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player2, 15);
    }

    @Test
    void cannotActivateAnAbilityWithoutEnoughChargeCounters() {
        addResearch();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addResearch() {
        return harness.addToBattlefieldAndReturn(player1, new FiremindsResearch());
    }
}
