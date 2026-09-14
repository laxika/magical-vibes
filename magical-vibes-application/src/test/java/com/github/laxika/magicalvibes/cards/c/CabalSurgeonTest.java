package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalSurgeon.class, CabalTorturer.class, CabalRitual.class})
class CabalSurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two graveyard cards and returns a target creature card to hand")
    void exilesTwoCardsAndReturnsTargetCreature() {
        Permanent surgeon = addReadySurgeon();
        Card creature = new CabalTorturer();
        Card other = new CabalRitual();
        Card third = new CabalRitual();
        harness.setGraveyard(player1, List.of(other, third, creature));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(other.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cabal Torturer");
        assertThat(surgeon.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactlyInAnyOrder(other.getId(), third.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Permanent surgeon = addReadySurgeon();
        Card target = new CabalRitual();
        Card costCard = new CabalRitual();
        Card otherCostCard = new CabalRitual();
        harness.setGraveyard(player1, List.of(target, costCard, otherCostCard));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot activate without two cards in the graveyard to exile")
    void cannotActivateWithoutTwoCardsToExile() {
        Permanent surgeon = addReadySurgeon();
        Card creature = new CabalTorturer();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Permanent surgeon = addReadySurgeon();
        Card target = new CabalTorturer();
        Card costCard = new CabalRitual();
        Card otherCostCard = new CabalRitual();
        harness.setGraveyard(player1, List.of(costCard, otherCostCard));
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(surgeon.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(costCard, otherCostCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent surgeon = addReadySurgeon();
        surgeon.tap();
        Card target = new CabalTorturer();
        Card costCard = new CabalRitual();
        Card otherCostCard = new CabalRitual();
        harness.setGraveyard(player1, List.of(target, costCard, otherCostCard));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target, costCard, otherCostCard);
    }

    @Test
    @DisplayName("A target exiled as part of the cost is not returned")
    void targetExiledAsCostIsNotReturned() {
        Permanent surgeon = addReadySurgeon();
        Card target = new CabalTorturer();
        Card costCard = new CabalRitual();
        Card remaining = new CabalRitual();
        harness.setGraveyard(player1, List.of(target, costCard, remaining));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(
                player1, index(surgeon), 0, List.of(target.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId(), costCard.getId()));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Cabal Torturer");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactlyInAnyOrder(target.getId(), costCard.getId());
        assertThat(surgeon.isTapped()).isTrue();
    }

    private Permanent addReadySurgeon() {
        return addCreatureReady(player1, new CabalSurgeon());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private int index(Permanent surgeon) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(surgeon);
    }
}
