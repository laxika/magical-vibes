package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.Aquamoeba;
import com.github.laxika.magicalvibes.cards.c.CabalSurgeon;
import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitchstoneWall.class, Aquamoeba.class, Unhinge.class, CabalSurgeon.class})
class PitchstoneWallTest extends BaseCardTest {

    @Test
    void acceptingMaySacrificesWallAndReturnsDiscardedCard() {
        Card discardedCard = prepareDiscard();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Pitchstone Wall");
        harness.assertNotInGraveyard(player1, discardedCard.getName());
        assertThat(gd.playerHands.get(player1.getId())).contains(discardedCard);
    }

    @Test
    void decliningMayKeepsWallAndLeavesDiscardedCardInGraveyard() {
        Card discardedCard = prepareDiscard();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Pitchstone Wall");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discardedCard);
    }

    @Test
    void opponentDiscardDoesNotTriggerWall() {
        harness.addToBattlefield(player2, new PitchstoneWall());
        Card discardedCard = new Aquamoeba();
        harness.setHand(player1, List.of(new Unhinge(), discardedCard));
        harness.setLibrary(player1, List.of(new Aquamoeba()));
        addUnhingeMana(player1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Pitchstone Wall");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    void discardAsActivationCostAlsoTriggersWall() {
        harness.addToBattlefield(player1, new Aquamoeba());
        harness.addToBattlefield(player1, new PitchstoneWall());
        Card discardedCard = new Aquamoeba();
        harness.setHand(player1, List.of(discardedCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pitchstone Wall");
        assertThat(gd.playerHands.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discardedCard);
    }

    @Test
    void cardThatReentersGraveyardBeforeTriggerResolvesIsNotReturned() {
        PitchstoneWall wall = new PitchstoneWall();
        CabalSurgeon surgeon = new CabalSurgeon();
        Permanent amoebaPermanent = addCreatureReady(player1, new Aquamoeba());
        harness.addToBattlefield(player1, wall);
        Permanent surgeonPermanent = addCreatureReady(player1, surgeon);

        Card discardedCard = new Aquamoeba();
        Card exileCostCard1 = new Aquamoeba();
        Card exileCostCard2 = new Aquamoeba();
        harness.setGraveyard(player1, List.of(exileCostCard1, exileCostCard2));
        harness.setHand(player1, List.of(new Unhinge(), discardedCard));
        harness.setLibrary(player1, List.of(new Aquamoeba()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Create Pitchstone Wall's trigger for the first discard.
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        // Move the discarded card to hand with Cabal Surgeon while its trigger waits on the stack.
        int surgeonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(surgeonPermanent);
        harness.activateAbilityWithGraveyardTargets(
                player1, surgeonIndex, 0, List.of(discardedCard.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exileCostCard1.getId(), exileCostCard2.getId()));
        harness.passBothPriorities();

        // Discard that card again as an activation cost, producing a new graveyard entry before the
        // original trigger resolves.
        int amoebaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(amoebaPermanent);
        harness.activateAbility(player1, amoebaIndex, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        int discardedCardHandIndex = gd.playerHands.get(player1.getId()).indexOf(discardedCard);
        harness.handleCardChosen(player1, discardedCardHandIndex);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Accept the original trigger. The card is in the graveyard, but it is not the original object.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discardedCard);
        harness.assertOnBattlefield(player1, "Pitchstone Wall");
    }

    private Card prepareDiscard() {
        harness.addToBattlefield(player1, new PitchstoneWall());
        Card discardedCard = new Aquamoeba();
        harness.setHand(player1, List.of(new Unhinge(), discardedCard));
        harness.setLibrary(player1, List.of(new Aquamoeba()));
        addUnhingeMana(player1);

        harness.castSorcery(player1, 0, player1.getId());
        return discardedCard;
    }

    private void addUnhingeMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
