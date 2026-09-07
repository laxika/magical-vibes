package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BrokkosApexOfForever.class)
class BrokkosApexOfForeverTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Brokkos from the graveyard using its mutate cost")
    void canCastFromGraveyardUsingMutateCost() {
        harness.setGraveyard(player1, List.of(new BrokkosApexOfForever()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brokkos, Apex of Forever");
    }

    @Test
    @DisplayName("Requires the mutate cost when cast from the graveyard")
    void requiresMutateCostFromGraveyard() {
        harness.setGraveyard(player1, List.of(new BrokkosApexOfForever()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
