package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitKeeper.class, GrizzlyBears.class, HolyDay.class})
class PitKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("With four creature cards in the graveyard, the ETB returns a chosen creature card")
    void returnsChosenCreatureCardWhenThresholdIsMet() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), target));

        castPitKeeper();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).contains(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The ETB does not trigger with fewer than four creature cards")
    void requiresFourCreatureCards() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new HolyDay()));

        castPitKeeper();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pit Keeper");
    }

    @Test
    @DisplayName("Only creature cards are legal graveyard targets")
    void onlyCreatureCardsAreTargetable() {
        HolyDay nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(
                nonCreature, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castPitKeeper();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).doesNotContain(nonCreature.getId()).hasSize(4);
    }

    @Test
    @DisplayName("Declining the optional return leaves the targeted card in the graveyard")
    void decliningReturnLeavesCardInGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), target));

        castPitKeeper();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private void castPitKeeper() {
        harness.setHand(player1, List.of(new PitKeeper()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
