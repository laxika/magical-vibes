package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarkhideMauler.class, Forest.class})
class BarkhideMaulerTest extends BaseCardTest {

    @Test
    void cyclingRequiresTwoGenericMana() {
        harness.setHand(player1, List.of(new BarkhideMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Barkhide Mauler");
    }

    @Test
    void cyclingDiscardsTheCardAndDrawsOne() {
        harness.setHand(player1, List.of(new BarkhideMauler()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Barkhide Mauler");
        harness.assertInHand(player1, "Forest");
    }
}
