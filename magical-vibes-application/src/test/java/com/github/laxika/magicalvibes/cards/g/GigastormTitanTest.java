package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GigastormTitan.class, Fog.class})
class GigastormTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot pay the full cost with only three generic mana")
    void cannotCastWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new GigastormTitan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs three less after another spell is cast this turn")
    void costsThreeLessAfterAnotherSpell() {
        harness.setHand(player1, List.of(new Fog(), new GigastormTitan()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Gigastorm Titan"));
    }
}
