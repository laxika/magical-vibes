package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunbeamSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("Paying white mana and sacrificing it gains 5 life")
    void whiteAbilityGainsLife() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Sunbeam Spellbomb");
        harness.assertInGraveyard(player1, "Sunbeam Spellbomb");
    }

    @Test
    @DisplayName("Paying one mana and sacrificing it draws a card")
    void colorlessAbilityDrawsCard() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Sunbeam Spellbomb");
        harness.assertInGraveyard(player1, "Sunbeam Spellbomb");
    }

    @Test
    @DisplayName("The white ability requires white mana")
    void whiteAbilityRequiresWhiteMana() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Sunbeam Spellbomb");
    }
}
