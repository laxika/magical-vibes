package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinChieftain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqueakingPieSneakTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Goblin in hand it costs {1}{B} plus the additional {3}")
    void requiresExtraThreeWithoutGoblin() {
        // The Sneak itself is a Goblin but is on the stack, so it cannot satisfy its own reveal.
        harness.setHand(player1, List.of(new SqueakingPieSneak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {3} can be paid with mana when no Goblin is revealed")
    void payTheThreeWithMana() {
        SqueakingPieSneak sneak = new SqueakingPieSneak();
        harness.setHand(player1, List.of(sneak));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4); // {1} + {3}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(sneak.getId()));
    }

    @Test
    @DisplayName("Revealing a Goblin card from hand lets it be cast for just {1}{B}")
    void revealGoblinAvoidsTheThree() {
        SqueakingPieSneak sneak = new SqueakingPieSneak();
        GoblinChieftain goblinInHand = new GoblinChieftain();
        harness.setHand(player1, List.of(sneak, goblinInHand));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(sneak.getId()));
        // Revealing does not remove the Goblin card from hand.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(goblinInHand.getId()));
    }
}
