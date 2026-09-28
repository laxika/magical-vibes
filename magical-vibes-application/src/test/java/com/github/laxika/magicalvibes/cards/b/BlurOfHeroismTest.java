package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlurOfHeroism.class, BraveBrawler.class, GrizzlyBears.class})
class BlurOfHeroismTest extends BaseCardTest {

    @Test
    @DisplayName("Blur of Heroism draws a card when it enters")
    void drawsCardOnEnter() {
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new BlurOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, libraryCard.getName());
        harness.assertOnBattlefield(player1, "Blur of Heroism");
    }

    @Test
    @DisplayName("Blur of Heroism lets its controller cast Hero spells during combat")
    void grantsFlashToHeroSpells() {
        harness.addToBattlefield(player1, new BlurOfHeroism());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BraveBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Blur of Heroism does not grant flash to non-Hero spells")
    void doesNotGrantFlashToNonHeroSpells() {
        harness.addToBattlefield(player1, new BlurOfHeroism());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
