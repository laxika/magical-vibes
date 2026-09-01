package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllvoiGaleblade.class, Forest.class})
class IllvoiGalebladeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} sacrifices Illvoi Galeblade and draws a card")
    void payingTwoSacrificesAndDraws() {
        harness.addToBattlefield(player1, new IllvoiGaleblade());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Illvoi Galeblade");
        harness.assertInGraveyard(player1, "Illvoi Galeblade");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Illvoi Galeblade cannot be activated without two mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new IllvoiGaleblade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Illvoi Galeblade");
    }
}
