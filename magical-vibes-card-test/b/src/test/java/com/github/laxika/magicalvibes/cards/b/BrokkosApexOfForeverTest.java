package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MosscoatGoriak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokkosApexOfForever.class, MosscoatGoriak.class})
class BrokkosApexOfForeverTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Brokkos from the graveyard without a mutate target")
    void cannotCastFromGraveyardWithoutMutateTarget() {
        harness.setGraveyard(player1, List.of(new BrokkosApexOfForever()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires the mutate cost when cast from the graveyard")
    void requiresMutateCostFromGraveyard() {
        var target = harness.addToBattlefieldAndReturn(player1, new MosscoatGoriak());
        harness.setGraveyard(player1, List.of(new BrokkosApexOfForever()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot mutate Brokkos from the graveyard onto a creature another player owns")
    void cannotMutateOntoOpponentsCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new MosscoatGoriak());
        harness.setGraveyard(player1, List.of(new BrokkosApexOfForever()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast Brokkos normally from hand without a mutate target")
    void canCastNormallyFromHand() {
        harness.castFromHand(player1, new BrokkosApexOfForever(), "{2}{B}{G}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brokkos, Apex of Forever");
    }
}
