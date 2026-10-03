package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarrionImp.class, CatacombCrocodile.class, ConsignToThePit.class})
class CarrionImpTest extends BaseCardTest {

    @Test
    void exilesCreatureCardAndGainsLife() {
        Card creature = new CatacombCrocodile();
        Card noncreature = new ConsignToThePit();
        harness.setGraveyard(player2, List.of(creature, noncreature));
        castCarrionImp();

        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void decliningMayAbilityDoesNothing() {
        Card creature = new CatacombCrocodile();
        harness.setGraveyard(player2, List.of(creature));
        castCarrionImp();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void doesNotTriggerWithoutCreatureCardTarget() {
        Card noncreature = new ConsignToThePit();
        harness.setGraveyard(player2, List.of(noncreature));
        castCarrionImp();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void canExileFromOwnGraveyardWithTargetsInBothGraveyards() {
        Card ownCreature = new CatacombCrocodile();
        Card opposingCreature = new CatacombCrocodile();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        castCarrionImp();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCreature.getId(), opposingCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainsNoLifeWhenTargetLeavesGraveyardBeforeResolution() {
        Card creature = new CatacombCrocodile();
        harness.setGraveyard(player2, List.of(creature));
        castCarrionImp();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        harness.assertLife(player1, 20);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Card creature = new CatacombCrocodile();
        harness.setGraveyard(player2, List.of(creature));
        castCarrionImp();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        Card imp = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(imp));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(imp);
        harness.assertLife(player1, 22);
    }

    private void castCarrionImp() {
        harness.castFromHand(player1, new CarrionImp(), "{3}{B}");
    }
}
