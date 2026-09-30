package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorealShelf;
import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimHarvest.class, KrovikanScoundrel.class, BorealShelf.class})
class GrimHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Grim Harvest returns a targeted creature card to its owner's hand")
    void returnsTargetedCreatureCard() {
        Card target = new KrovikanScoundrel();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new GrimHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        harness.assertInGraveyard(player1, "Grim Harvest");
    }

    @Test
    @DisplayName("Grim Harvest cannot target a noncreature card in its controller's graveyard")
    void cannotTargetNoncreatureCardInOwnGraveyard() {
        Card nonCreature = new GrimHarvest();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new GrimHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target card must be a creature");
    }

    @Test
    @DisplayName("Grim Harvest cannot target a creature card in an opponent's graveyard")
    void cannotTargetCreatureCardInOpponentGraveyard() {
        Card opponentsCreature = new KrovikanScoundrel();
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(new GrimHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentsCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Grim Harvest recover returns it to its owner's hand when paid")
    void recoverReturnsSourceWhenPaid() {
        Card harvest = new GrimHarvest();
        harness.setGraveyard(player1, List.of(harvest));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KrovikanScoundrel());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(harvest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(harvest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(harvest);
    }

    @Test
    @DisplayName("Grim Harvest recover exiles it when declined")
    void recoverExilesSourceWhenDeclined() {
        Card harvest = new GrimHarvest();
        harness.setGraveyard(player1, List.of(harvest));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KrovikanScoundrel());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(harvest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(harvest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(harvest);
    }

    @Test
    @DisplayName("Grim Harvest recover does not trigger for an opponent's creature")
    void recoverDoesNotTriggerForOpponentCreature() {
        Card harvest = new GrimHarvest();
        harness.setGraveyard(player1, List.of(harvest));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(harvest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(harvest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(harvest);
    }

    @Test
    @DisplayName("Grim Harvest recover does not trigger for a noncreature permanent")
    void recoverDoesNotTriggerForNoncreature() {
        Card harvest = new GrimHarvest();
        harness.setGraveyard(player1, List.of(harvest));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BorealShelf());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(harvest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(harvest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(harvest);
    }
}
