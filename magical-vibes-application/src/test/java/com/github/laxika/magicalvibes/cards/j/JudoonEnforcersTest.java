package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JudoonEnforcers.class, GiantCockroach.class})
class JudoonEnforcersTest extends BaseCardTest {

    @Test
    @DisplayName("No more than one creature can attack its controller")
    void limitsAttacksAgainstController() {
        harness.addToBattlefield(player2, new JudoonEnforcers());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
    }

    @Test
    @DisplayName("One creature can attack its controller")
    void allowsOneAttackAgainstController() {
        harness.addToBattlefield(player2, new JudoonEnforcers());
        addCreatureReady(player1, new GiantCockroach());

        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Suspend exiles Judoon Enforcers with six time counters")
    void suspendExilesWithSixTimeCounters() {
        JudoonEnforcers card = new JudoonEnforcers();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.stack).isEmpty();
    }
}
