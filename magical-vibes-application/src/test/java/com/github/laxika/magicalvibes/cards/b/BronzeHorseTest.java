package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BronzeHorseTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from a spell that targets it while you control another creature")
    void preventsTargetingSpellDamageWithAnotherCreature() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, horse.getId());
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Bronze Horse");
    }

    @Test
    @DisplayName("Does not prevent targeted spell damage without another creature")
    void doesNotPreventTargetingSpellDamageWithoutAnotherCreature() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, horse.getId());
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not prevent damage from an ability")
    void doesNotPreventAbilityDamage() {
        Permanent horse = addCreatureReady(player2, new BronzeHorse());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent triskelion = addCreatureReady(player1, new Triskelion());
        triskelion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, horse.getId());
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isEqualTo(1);
    }
}
