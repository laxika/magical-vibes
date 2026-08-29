package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Repeal.class, GrizzlyBears.class, HillGiant.class, Plains.class})
class RepealTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonland permanent with mana value X and draws a card")
    void returnsMatchingPermanentAndDrawsCard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Repeal()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 2, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Repeal");
    }

    @Test
    @DisplayName("Cannot target a permanent with a different mana value")
    void cannotTargetDifferentManaValue() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new Repeal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent with mana value X");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Plains()).getId();
        harness.setHand(player1, List.of(new Repeal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent with mana value X");
    }
}
