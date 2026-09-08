package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheGoldSaucer.class, Spellbook.class})
class TheGoldSaucerTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(saucer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability creates a Treasure only when the coin flip is won")
    void coinFlipCreatesTreasureOnWin() {
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip");
        boolean lost = gameLogContains("loses the coin flip");
        assertThat(won != lost).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(won ? 1 : 0);
        assertThat(saucer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Third ability sacrifices two artifacts and draws a card")
    void sacrificesTwoArtifactsAndDraws() {
        harness.addToBattlefield(player1, new TheGoldSaucer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spellbook")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Spellbook")))
                .hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Third ability cannot activate without two artifacts")
    void cannotSacrificeTwoArtifactsWithoutTwoArtifacts() {
        harness.addToBattlefield(player1, new TheGoldSaucer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
