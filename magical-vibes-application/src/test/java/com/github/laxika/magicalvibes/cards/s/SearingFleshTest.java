package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
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

@CardUsed({SearingFlesh.class, ElspethKnightErrant.class, ElvishWarrior.class})
class SearingFleshTest extends BaseCardTest {

    private void giveSearingFlesh() {
        harness.setHand(player1, List.of(new SearingFlesh()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    @Test
    @DisplayName("Deals 7 damage to target opponent")
    void dealsDamageToOpponent() {
        giveSearingFlesh();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Deals 7 damage to target opponent's planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        giveSearingFlesh();

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals 7 damage to a planeswalker controlled by the caster")
    void dealsDamageToOwnPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        giveSearingFlesh();

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target yourself or a creature")
    void rejectsIllegalTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        giveSearingFlesh();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
