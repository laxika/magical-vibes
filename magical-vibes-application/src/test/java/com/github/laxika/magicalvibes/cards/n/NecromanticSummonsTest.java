package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecromanticSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Reanimates without counters when spell mastery is not active")
    void reanimatesWithoutSpellMastery() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new HolyDay()));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findOnBattlefield(creature);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Spell mastery adds two +1/+1 counters with two instants in the graveyard")
    void spellMasteryAddsCounters() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new HolyDay(), new HolyDay()));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findOnBattlefield(creature).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reanimates a creature from an opponent's graveyard under your control")
    void reanimatesFromOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findOnBattlefield(creature)).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a non-creature card in a graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findOnBattlefield(Card card) {
        GameData gd = harness.getGameData();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(card.getId()))
                .findFirst().orElseThrow();
    }
}
