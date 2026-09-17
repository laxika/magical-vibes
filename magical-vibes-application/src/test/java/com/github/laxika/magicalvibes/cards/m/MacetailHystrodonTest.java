package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MacetailHystrodon.class, FugitiveWizard.class})
class MacetailHystrodonTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards Macetail Hystrodon and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MacetailHystrodon()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Macetail Hystrodon");
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cycling cannot be activated without three mana")
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new MacetailHystrodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Macetail Hystrodon");
    }
}
