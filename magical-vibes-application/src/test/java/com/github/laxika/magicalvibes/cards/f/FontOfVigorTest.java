package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FontOfVigor.class})
class FontOfVigorTest extends BaseCardTest {

    @Test
    @DisplayName("sacrificing Font of Vigor gains 7 life")
    void sacrificingFontOfVigorGainsSevenLife() {
        harness.setLife(player1, 10);
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfVigor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == font.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("activation requires white mana and does not sacrifice the Font on failure")
    void cannotActivateWithoutWhiteMana() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfVigor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(font.getCard());
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("activation requires all three mana")
    void cannotActivateWithOnlyTwoMana() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfVigor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(font.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("a tapped Font can be activated on an opponent's turn")
    void tappedFontCanBeActivatedOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfVigor());
        font.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 27);
        harness.assertLife(player2, 20);
    }
}
