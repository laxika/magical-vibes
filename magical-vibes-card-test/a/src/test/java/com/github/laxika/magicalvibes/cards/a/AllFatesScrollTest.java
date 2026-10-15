package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AllFatesScroll.class, Forest.class, Island.class, Mountain.class})
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
        harness.setLibrary(player1, List.of(new AllFatesScroll(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("All-Fates Scroll", "Island", "Mountain");
        harness.assertInGraveyard(player1, "All-Fates Scroll");
    }

    @Test
    void drawsNothingWithoutControlledLands() {
        harness.addToBattlefield(player1, new AllFatesScroll());
        harness.addToBattlefield(player1, new AllFatesScroll());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "All-Fates Scroll");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void countsLandNamesAtResolutionAndIgnoresNonlands() {
        harness.addToBattlefield(player1, new AllFatesScroll());
        harness.addToBattlefield(player1, new AllFatesScroll());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateDrawAbilityWithInsufficientMana() {
        harness.addToBattlefield(player1, new AllFatesScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "All-Fates Scroll");
        harness.assertNotInGraveyard(player1, "All-Fates Scroll");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDrawAbilityAfterTappingForMana() {
        harness.addToBattlefield(player1, new AllFatesScroll());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "All-Fates Scroll");
        harness.assertNotInGraveyard(player1, "All-Fates Scroll");
    }
}
