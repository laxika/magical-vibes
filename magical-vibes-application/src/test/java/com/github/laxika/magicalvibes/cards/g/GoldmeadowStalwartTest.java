package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoldmeadowStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Kithkin in hand it costs {W} plus the additional {3}")
    void requiresExtraThreeWithoutKithkin() {
        // The Stalwart itself is a Kithkin but is on the stack, so it cannot satisfy its own reveal.
        harness.setHand(player1, List.of(new GoldmeadowStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {3} can be paid with mana when no Kithkin is revealed")
    void payTheThreeWithMana() {
        GoldmeadowStalwart stalwart = new GoldmeadowStalwart();
        harness.setHand(player1, List.of(stalwart));
        harness.addMana(player1, ManaColor.WHITE, 4); // {W} + {3}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(stalwart.getId()));
    }

    @Test
    @DisplayName("Revealing a Kithkin card from hand lets it be cast for just {W}")
    void revealKithkinAvoidsTheThree() {
        GoldmeadowStalwart stalwart = new GoldmeadowStalwart();
        GoldmeadowHarrier kithkinInHand = new GoldmeadowHarrier();
        harness.setHand(player1, List.of(stalwart, kithkinInHand));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(stalwart.getId()));
        // Revealing does not remove the Kithkin card from hand.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(kithkinInHand.getId()));
    }
}
