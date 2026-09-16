package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToxicDeluge.class, GrizzlyBears.class})
class ToxicDelugeTest extends BaseCardTest {

    @Test
    @DisplayName("Pays X life and gives all creatures -X/-X")
    void paysLifeAndShrinksAllCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(ownBear.getPowerModifier()).isEqualTo(-2);
        assertThat(ownBear.getToughnessModifier()).isEqualTo(-2);
        assertThat(opposingBear.getPowerModifier()).isEqualTo(-2);
        assertThat(opposingBear.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("X=0 pays no life and leaves creatures unchanged")
    void zeroXPaysNoLifeAndDoesNotShrinkCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast when X life cannot be paid")
    void cannotPayMoreLifeThanAvailable() {
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
    }
}
