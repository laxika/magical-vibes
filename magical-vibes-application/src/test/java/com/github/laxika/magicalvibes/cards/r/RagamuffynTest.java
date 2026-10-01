package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GuardianOfTheGuildpact;
import com.github.laxika.magicalvibes.cards.h.HallowedFountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ragamuffyn.class, HallowedFountain.class, GuardianOfTheGuildpact.class})
class RagamuffynTest extends BaseCardTest {

    @Test
    @DisplayName("With an empty hand, the ability can sacrifice Ragamuffyn and draw a card")
    void canSacrificeItselfAndDraw() {
        addCreatureReady(player1, new Ragamuffyn());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HallowedFountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ragamuffyn");
        harness.assertInGraveyard(player1, "Ragamuffyn");
        harness.assertInHand(player1, "Hallowed Fountain");
    }

    @Test
    @DisplayName("The ability can sacrifice a land and draw a card")
    void canSacrificeLandAndDraw() {
        Permanent ragamuffyn = addCreatureReady(player1, new Ragamuffyn());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new HallowedFountain());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HallowedFountain()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(ragamuffyn.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Ragamuffyn");
        harness.assertInGraveyard(player1, "Hallowed Fountain");
        harness.assertInHand(player1, "Hallowed Fountain");
    }

    @Test
    @DisplayName("The ability can sacrifice another creature and draw a card")
    void canSacrificeAnotherCreatureAndDraw() {
        Permanent ragamuffyn = addCreatureReady(player1, new Ragamuffyn());
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheGuildpact());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HallowedFountain()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, guardian.getId());
        harness.passBothPriorities();

        assertThat(ragamuffyn.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Ragamuffyn");
        harness.assertInGraveyard(player1, "Guardian of the Guildpact");
        harness.assertInHand(player1, "Hallowed Fountain");
    }

    @Test
    @DisplayName("The ability cannot be activated with cards in hand")
    void requiresEmptyHand() {
        addCreatureReady(player1, new Ragamuffyn());
        harness.setHand(player1, List.of(new HallowedFountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no cards in hand");
    }
}
