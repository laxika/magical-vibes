package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DivineReckoning;
import com.github.laxika.magicalvibes.cards.n.Nevermore;
import com.github.laxika.magicalvibes.cards.p.PathOfPeace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimpseTheUnthinkablePlaytest.class, PathOfPeace.class, DivineReckoning.class, Nevermore.class})
class GlimpseTheUnthinkablePlaytestTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents spells from targeting Glimpse, the Unthinkable")
    void shroudPreventsTargeting() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(
                player1, new GlimpseTheUnthinkablePlaytest());

        harness.setHand(player2, List.of(new PathOfPeace()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, glimpse.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Divine Reckoning cannot keep Glimpse even when it is the only creature")
    void cannotBeChosenToSurviveDivineReckoning() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new DivineReckoning(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player1, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Glimpse's name cannot be chosen while it is on the battlefield")
    void cannotChooseNameWhileOnBattlefield() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new Nevermore(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Glimpse, the Unthinkable"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertNotOnBattlefield(player1, "Nevermore");
    }

    @Test
    @DisplayName("Glimpse's name can be chosen while it is only in a player's hand")
    void canChooseNameWhileOffBattlefield() {
        harness.setHand(player2, List.of(new GlimpseTheUnthinkablePlaytest()));

        harness.castFromHand(player1, new Nevermore(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Glimpse, the Unthinkable");

        harness.assertOnBattlefield(player1, "Nevermore");
        assertThat(findPermanent(player1, "Nevermore").getChosenName())
                .isEqualTo("Glimpse, the Unthinkable");
    }
}
