package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampiricTouch.class, AirElemental.class, JaceBeleren.class})
class VampiricTouchTest extends BaseCardTest {

    private void addManaForVampiricTouch() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Deals 2 damage to target opponent and controller gains 2 life")
    void dealsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new VampiricTouch()));
        addManaForVampiricTouch();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Deals 2 damage to a planeswalker it controls and controller gains 2 life")
    void dealsDamageToPlaneswalkerAndGainsLife() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new VampiricTouch()));
        addManaForVampiricTouch();

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target yourself — only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new VampiricTouch()));
        addManaForVampiricTouch();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature — only opponent or planeswalker")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new VampiricTouch()));
        addManaForVampiricTouch();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
