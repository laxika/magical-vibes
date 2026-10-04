package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MomentaryBlink;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallowWurm.class, Forest.class, GrizzlyBears.class, MomentaryBlink.class, Unsummon.class})
class FallowWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a land card keeps Fallow Wurm on the battlefield")
    void discardingLandKeepsWurm() {
        resolveWurmWith(List.of(new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the discard sacrifices Fallow Wurm")
    void decliningSacrificesWurm() {
        resolveWurmWith(List.of(new Forest()));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Fallow Wurm");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Auto-sacrifices when the controller has no land card in hand")
    void autoSacrificesWithoutLand() {
        harness.castFromHand(player1, new FallowWurm(), "{2}{G}");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Fallow Wurm");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only land cards are offered for the discard")
    void onlyLandsAreValidDiscards() {
        resolveWurmWith(List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactlyInAnyOrder(1, 3);
    }

    @Test
    @DisplayName("Discards only the selected land when multiple lands are in hand")
    void discardsOnlySelectedLand() {
        resolveWurmWith(List.of(new GrizzlyBears(), new Forest(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 2);

        harness.assertOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("An empty controller hand cannot use the opponent's land to keep the Wurm")
    void emptyHandSacrificesDespiteOpponentsLand() {
        harness.setHand(player2, List.of(new Forest()));
        harness.castFromHand(player1, new FallowWurm(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Fallow Wurm");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may still discard a land after the Wurm leaves the battlefield")
    void mayDiscardAfterWurmLeaves() {
        harness.castFromHand(player1, new FallowWurm(), "{2}{G}");
        harness.setHand(player1, List.of(new Unsummon(), new Forest()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Fallow Wurm"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Fallow Wurm");
        harness.assertInHand(player1, "Fallow Wurm");
        harness.assertNotInGraveyard(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The original enters trigger cannot sacrifice the Wurm returned by Momentary Blink")
    void originalTriggerCannotSacrificeReturnedWurm() {
        harness.castFromHand(player1, new FallowWurm(), "{2}{G}");
        harness.setHand(player1, List.of(new MomentaryBlink(), new Forest()));
        harness.passBothPriorities();
        var originalPermanentId = harness.getPermanentId(player1, "Fallow Wurm");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, originalPermanentId);
        assertThat(harness.getPermanentId(player1, "Fallow Wurm")).isNotEqualTo(originalPermanentId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Fallow Wurm");
        harness.assertInGraveyard(player1, "Forest");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fallow Wurm");
        harness.assertNotInGraveyard(player1, "Fallow Wurm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    /**
     * Casts Fallow Wurm, swaps the controller's hand for {@code hand}, and resolves the
     * creature spell and its enters-the-battlefield trigger up to the may-ability prompt.
     */
    private void resolveWurmWith(List<com.github.laxika.magicalvibes.model.Card> hand) {
        harness.castFromHand(player1, new FallowWurm(), "{2}{G}");
        harness.setHand(player1, hand);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
