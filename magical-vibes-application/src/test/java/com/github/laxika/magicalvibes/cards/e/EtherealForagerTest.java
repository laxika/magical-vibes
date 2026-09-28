package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealForager.class, Shock.class, Ponder.class, GrizzlyBears.class})
class EtherealForagerTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(graveyard));
        harness.setHand(player1, List.of(new EtherealForager()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof EtherealForager);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyard.get(4));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard.subList(0, 4));
    }

    @Test
    @DisplayName("The attack trigger may return one exiled instant or sorcery")
    void attackTriggerReturnsEligibleCard() {
        Permanent forager = addCreatureReady(player1, new EtherealForager());
        Card shock = new Shock();
        Card creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), shock, forager.getId());
        gd.addToExile(player1.getId(), creature, forager.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.getCardsExiledByPermanent(forager.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("The attack trigger filters the choice to instants and sorceries")
    void attackTriggerChoiceExcludesOtherCardTypes() {
        Permanent forager = addCreatureReady(player1, new EtherealForager());
        Card shock = new Shock();
        Card ponder = new Ponder();
        Card creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), shock, forager.getId());
        gd.addToExile(player1.getId(), ponder, forager.getId());
        gd.addToExile(player1.getId(), creature, forager.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(shock, ponder);

        harness.handleMultipleCardsChosen(player1, List.of(ponder.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(ponder);
        assertThat(gd.getCardsExiledByPermanent(forager.getId()))
                .containsExactlyInAnyOrder(shock, creature);
    }
}
