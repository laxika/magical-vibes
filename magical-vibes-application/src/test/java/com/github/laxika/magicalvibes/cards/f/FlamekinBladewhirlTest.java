package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlamekinBladewhirlTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Elemental in hand it costs {R} plus the additional {3}")
    void requiresExtraThreeWithoutElemental() {
        // Bladewhirl is itself an Elemental but is on the stack, so it cannot satisfy its own reveal.
        harness.setHand(player1, List.of(new FlamekinBladewhirl()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {3} can be paid with mana when no Elemental is revealed")
    void payTheThreeWithMana() {
        FlamekinBladewhirl bladewhirl = new FlamekinBladewhirl();
        harness.setHand(player1, List.of(bladewhirl));
        harness.addMana(player1, ManaColor.RED, 4); // {R} + {3}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bladewhirl.getId()));
    }

    @Test
    @DisplayName("Revealing an Elemental card from hand lets it be cast for just {R}")
    void revealElementalAvoidsTheThree() {
        FlamekinBladewhirl bladewhirl = new FlamekinBladewhirl();
        FlamekinBladewhirl elementalInHand = new FlamekinBladewhirl();
        harness.setHand(player1, List.of(bladewhirl, elementalInHand));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bladewhirl.getId()));
        // Revealing does not remove the Elemental card from hand.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(elementalInHand.getId()));
    }
}
