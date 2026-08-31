package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ragamuffyn.class, Forest.class})
class RagamuffynTest extends BaseCardTest {

    @Test
    @DisplayName("With an empty hand, the ability can sacrifice Ragamuffyn and draw a card")
    void canSacrificeItselfAndDraw() {
        addCreatureReady(player1, new Ragamuffyn());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ragamuffyn");
        harness.assertInGraveyard(player1, "Ragamuffyn");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The ability can sacrifice a land and draw a card")
    void canSacrificeLandAndDraw() {
        addCreatureReady(player1, new Ragamuffyn());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ragamuffyn");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The ability cannot be activated with cards in hand")
    void requiresEmptyHand() {
        addCreatureReady(player1, new Ragamuffyn());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no cards in hand");
    }
}
