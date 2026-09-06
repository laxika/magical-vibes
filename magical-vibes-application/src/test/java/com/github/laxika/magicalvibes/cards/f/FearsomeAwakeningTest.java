package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.PearlDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FearsomeAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a non-Dragon creature with no counters")
    void returnsNonDragonWithoutCounters() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new FearsomeAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findOnBattlefield(creature);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Returns a Dragon with two +1/+1 counters")
    void returnsDragonWithTwoCounters() {
        Card dragon = new PearlDragon();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of(new FearsomeAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, dragon.getId());
        harness.passBothPriorities();

        assertThat(findOnBattlefield(dragon).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new FearsomeAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new FearsomeAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    private Permanent findOnBattlefield(Card card) {
        return harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(card.getId()))
                .findFirst().orElseThrow();
    }
}
