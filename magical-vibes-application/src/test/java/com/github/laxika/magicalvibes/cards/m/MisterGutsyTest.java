package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisterGutsy.class, HolyStrength.class, LeoninScimitar.class, GrizzlyBears.class, Murder.class})
class MisterGutsyTest extends BaseCardTest {

    @Test
    void putsCountersOnItselfForAuraAndEquipmentSpells() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());

        harness.setHand(player1, List.of(new HolyStrength(), new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, gutsy.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gutsy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void ignoresNonAuraAndNonEquipmentSpells() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gutsy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void createsJunkForEachPlusOnePlusOneCounterWhenItDies() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());

        harness.setHand(player1, List.of(new HolyStrength(), new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, gutsy.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, gutsy.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void createsNoJunkWhenItDiesWithoutCounters() {
        Permanent gutsy = harness.addToBattlefieldAndReturn(player1, new MisterGutsy());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, gutsy.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isZero();
    }
}
