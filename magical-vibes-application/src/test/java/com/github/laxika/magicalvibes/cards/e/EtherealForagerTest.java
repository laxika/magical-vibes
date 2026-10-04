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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealForager.class, Shock.class, Ponder.class, GrizzlyBears.class})
class EtherealForagerTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
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

    @Test
    @DisplayName("The attack trigger returns a card actually exiled to pay delve")
    void attackReturnsCardUsedForDelve() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new EtherealForager()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();
        Permanent forager = findPermanent(player1, "Ethereal Forager");
        forager.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the linked card in exile")
    void attackReturnMayBeDeclined() {
        Permanent forager = addCreatureReady(player1, new EtherealForager());
        Card ponder = new Ponder();
        gd.addToExile(player1.getId(), ponder, forager.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(forager.getId())).containsExactly(ponder);
    }

    @Test
    @DisplayName("The attack trigger does nothing when only a creature is linked")
    void attackWithoutEligibleCardsDoesNothing() {
        Permanent forager = addCreatureReady(player1, new EtherealForager());
        Card creature = new GrizzlyBears();
        Card unrelated = new Shock();
        gd.addToExile(player1.getId(), creature, forager.getId());
        harness.setExile(player1, List.of(unrelated));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(creature, unrelated);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    @Test
    @DisplayName("The attack trigger cannot return cards exiled with another Forager")
    void attackOnlyReturnsCardsLinkedToAttackingForager() {
        Permanent attacker = addCreatureReady(player1, new EtherealForager());
        Permanent other = addCreatureReady(player1, new EtherealForager());
        Card shock = new Shock();
        Card ponder = new Ponder();
        gd.addToExile(player1.getId(), shock, attacker.getId());
        gd.addToExile(player1.getId(), ponder, other.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getCardsExiledByPermanent(other.getId())).containsExactly(ponder);
    }

    @Test
    @DisplayName("A new controller may return a delved card to its original owner's hand")
    void newControllerReturnsDelvedCardToOwner() {
        Card ponder = new Ponder();
        harness.setGraveyard(player1, List.of(ponder));
        harness.setHand(player1, List.of(new EtherealForager()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();
        Permanent forager = findPermanent(player1, "Ethereal Forager");
        gd.playerBattlefields.get(player1.getId()).remove(forager);
        gd.playerBattlefields.get(player2.getId()).add(forager);
        forager.setSummoningSick(false);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ponder);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
