package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SeaGateWreckage.class, GrizzlyBears.class})
class SeaGateWreckageTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        Permanent wreckage = addWreckage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(wreckage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws a card when the controller has no cards in hand")
    void drawsWhenHandEmpty() {
        Permanent wreckage = addWreckage();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(wreckage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the draw ability while the controller holds a card")
    void cannotDrawWithCardsInHand() {
        Permanent wreckage = addWreckage();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no cards in hand");

        assertThat(wreckage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires a colorless mana for the draw ability")
    void requiresColorlessMana() {
        addWreckage();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addWreckage() {
        Permanent wreckage = new Permanent(new SeaGateWreckage());
        wreckage.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wreckage);
        return wreckage;
    }
}
