package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WitchsCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gains 1 life and draws a card")
    void sacrificeCreatureGainsLifeAndDrawsCard() {
        harness.addToBattlefield(player1, new WitchsCauldron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        GameData gameData = harness.getGameData();
        int handSizeBefore = gameData.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Witch's Cauldron cannot be activated without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new WitchsCauldron());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
