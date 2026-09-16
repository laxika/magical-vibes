package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hundroog.class, AvenEnvoy.class})
class HundroogTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards Hundroog and draws a card")
    void cyclingDiscardsHundroogAndDraws() {
        harness.setHand(player1, List.of(new Hundroog()));
        harness.setLibrary(player1, List.of(new AvenEnvoy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hundroog");
        harness.assertInHand(player1, "Aven Envoy");
    }

    @Test
    @DisplayName("Cycling cannot be activated without three generic mana")
    void cyclingRequiresThreeGenericMana() {
        Hundroog hundroog = new Hundroog();
        harness.setHand(player1, List.of(hundroog));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hundroog);
        harness.assertNotInGraveyard(player1, "Hundroog");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
