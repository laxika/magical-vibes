package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshmouthDragon;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmolderingEgg.class, AshmouthDragon.class, Pyroclasm.class, Shock.class})
class SmolderingEggTest extends BaseCardTest {

    @Test
    @DisplayName("Puts ember counters equal to mana spent on an instant or sorcery")
    void putsCountersEqualToManaSpent() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.EMBER)).isEqualTo(2);
        assertThat(egg.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Removes ember counters and transforms at seven counters")
    void transformsAtSevenCounters() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        egg.setCounterCount(CounterType.EMBER, 6);
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(egg.isTransformed()).isTrue();
        assertThat(egg.getCard()).isInstanceOf(AshmouthDragon.class);
        assertThat(egg.getCounterCount(CounterType.EMBER)).isZero();
    }

    @Test
    @DisplayName("Ashmouth Dragon deals 2 damage to a target when an instant or sorcery is cast")
    void backFaceDealsDamageToAnyTarget() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        dragon.setCard(dragon.getOriginalCard().getBackFaceCard());
        dragon.setTransformed(true);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }
}
