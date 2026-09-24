package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BenalishHeralds.class)
class BenalishHeraldsTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {3}{U} and taps to draw a card")
    void paysManaAndTapsToDrawACard() {
        Permanent heralds = addCreatureReady(player1, new BenalishHeralds());
        harness.setLibrary(player1, List.of(new BenalishHeralds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        assertThat(heralds.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent heralds = addCreatureReady(player1, new BenalishHeralds());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(heralds.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent heralds = harness.addToBattlefieldAndReturn(player1, new BenalishHeralds());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(heralds.isTapped()).isFalse();
    }
}
