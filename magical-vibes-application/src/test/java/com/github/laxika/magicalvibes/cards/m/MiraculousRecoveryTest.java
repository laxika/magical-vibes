package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.j.JamuraanLion;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MiraculousRecovery.class, JamuraanLion.class})
class MiraculousRecoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature with a +1/+1 counter")
    void returnsCreatureWithCounter() {
        Card creature = new JamuraanLion();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MiraculousRecovery()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, creature.getName());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, creature.getName());
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new MiraculousRecovery();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new MiraculousRecovery()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new JamuraanLion();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new MiraculousRecovery()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Does nothing if the target creature leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new JamuraanLion();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MiraculousRecovery()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, creature.getName());
    }
}
