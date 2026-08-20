package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MurderousCut;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpectacleMageTest extends BaseCardTest {

    @Test
    void reducesHighManaValueSorceryCost() {
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesHighManaValueInstantCost() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceLowerManaValueSpells() {
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player1, List.of(new Concentrate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceOpponentsSpells() {
        harness.addToBattlefield(player1, new SpectacleMage());
        harness.setHand(player2, List.of(new TimeWarp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
