package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WrensRunVanquisherTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Elf in hand it costs {1}{G} plus the additional {3}")
    void requiresExtraThreeWithoutElf() {
        // The Vanquisher itself is an Elf but is on the stack, so it cannot satisfy its own reveal.
        harness.setHand(player1, List.of(new WrensRunVanquisher()));
        harness.addMana(player1, ManaColor.GREEN, 2); // {1}{G} only

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {3} can be paid with mana when no Elf is revealed")
    void payTheThreeWithMana() {
        WrensRunVanquisher vanquisher = new WrensRunVanquisher();
        harness.setHand(player1, List.of(vanquisher));
        harness.addMana(player1, ManaColor.GREEN, 5); // {1}{G} + {3}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(vanquisher.getId()));
    }

    @Test
    @DisplayName("Revealing an Elf card from hand lets it be cast for just {1}{G}")
    void revealElfAvoidsTheThree() {
        WrensRunVanquisher vanquisher = new WrensRunVanquisher();
        ElvishEulogist elfInHand = new ElvishEulogist();
        harness.setHand(player1, List.of(vanquisher, elfInHand));
        harness.addMana(player1, ManaColor.GREEN, 2); // {1}{G} only

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(vanquisher.getId()));
        // Revealing does not remove the Elf card from hand.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(elfInHand.getId()));
    }
}
