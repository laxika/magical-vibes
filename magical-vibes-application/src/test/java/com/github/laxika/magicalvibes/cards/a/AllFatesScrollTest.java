package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllFatesScroll.class, Forest.class, Island.class, Mountain.class, GrizzlyBears.class})
class AllFatesScrollTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one mana of the chosen color")
    void tapsForAnyColorMana() {
        harness.addToBattlefield(player1, new AllFatesScroll());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing it draws cards for differently named lands you control")
    void sacrificesAndDrawsForDifferentlyNamedControlledLands() {
        harness.addToBattlefield(player1, new AllFatesScroll());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Island", "Mountain");
        harness.assertInGraveyard(player1, "All-Fates Scroll");
    }
}
