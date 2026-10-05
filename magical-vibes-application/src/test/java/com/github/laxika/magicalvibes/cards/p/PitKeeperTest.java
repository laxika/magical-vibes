package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DreadReturn;
import com.github.laxika.magicalvibes.cards.d.DrudgeReavers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitKeeper.class, DrudgeReavers.class, DreadReturn.class})
class PitKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("With four or more creature cards in the graveyard, the ETB returns a chosen creature card")
    void returnsChosenCreatureCardWhenThresholdIsMet() {
        DrudgeReavers target = new DrudgeReavers();
        harness.setGraveyard(player1, List.of(
                new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers(), target,
                new DrudgeReavers()));

        castPitKeeper();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).contains(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Drudge Reavers");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The ETB does not trigger with fewer than four creature cards")
    void requiresFourCreatureCards() {
        harness.setGraveyard(player1, List.of(
                new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers(), new DreadReturn()));

        castPitKeeper();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pit Keeper");
    }

    @Test
    @DisplayName("Only creature cards are legal graveyard targets")
    void onlyCreatureCardsAreTargetable() {
        DreadReturn nonCreature = new DreadReturn();
        harness.setGraveyard(player1, List.of(
                nonCreature, new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers()));

        castPitKeeper();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).doesNotContain(nonCreature.getId()).hasSize(4);
    }

    @Test
    @DisplayName("Declining the optional return leaves the targeted card in the graveyard")
    void decliningReturnLeavesCardInGraveyard() {
        DrudgeReavers target = new DrudgeReavers();
        harness.setGraveyard(player1, List.of(
                new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers(), target));

        castPitKeeper();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        harness.assertNotInHand(player1, "Drudge Reavers");
    }

    @Test
    @DisplayName("The ETB counts only creature cards in its controller's graveyard")
    void countsOnlyControllerGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(
                new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers()));

        castPitKeeper();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pit Keeper");
    }

    @Test
    @DisplayName("The return does nothing if fewer than four creatures remain at resolution")
    void rechecksCreatureThresholdAtResolution() {
        DrudgeReavers target = new DrudgeReavers();
        DrudgeReavers second = new DrudgeReavers();
        DrudgeReavers third = new DrudgeReavers();
        harness.setGraveyard(player1, List.of(target, second, third, new DrudgeReavers()));

        castPitKeeper();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(target, second, third));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Drudge Reavers");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the graveyard cannot be replaced with another creature")
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        DrudgeReavers target = new DrudgeReavers();
        List<DrudgeReavers> remaining = List.of(
                new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers());
        harness.setGraveyard(player1, List.of(
                target, remaining.get(0), remaining.get(1), remaining.get(2), remaining.get(3)));

        castPitKeeper();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(
                remaining.get(0), remaining.get(1), remaining.get(2), remaining.get(3)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Drudge Reavers");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(remaining);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures in an opponent's graveyard are not legal return targets")
    void cannotTargetOpponentGraveyard() {
        DrudgeReavers opponentCard = new DrudgeReavers();
        harness.setGraveyard(player1, List.of(
                new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers(), new DrudgeReavers()));
        harness.setGraveyard(player2, List.of(opponentCard));

        castPitKeeper();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).hasSize(4).doesNotContain(opponentCard.getId());
    }

    private void castPitKeeper() {
        harness.castFromHand(player1, new PitKeeper(), "{1}{B}");
        harness.passBothPriorities();
    }
}
