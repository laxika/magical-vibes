package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmoredSkyhunter.class, GrizzlyBears.class, HolyStrength.class, LeoninScimitar.class, Shock.class})
class ArmoredSkyhunterTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, it puts Auras and Equipment from the top six onto the battlefield")
    void attacksAndAttachesEquipmentToAControlledCreature() {
        Permanent skyhunter = addCreatureReady(player1, new ArmoredSkyhunter());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Card aura = new HolyStrength();
        Card equipment = new LeoninScimitar();
        Card nonmatching = new Shock();
        harness.setLibrary(player1, List.of(aura, equipment, nonmatching));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(aura.getId(), equipment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice).isNotNull();
        assertThat(creatureChoice.validIds()).containsExactly(skyhunter.getId(), bear.getId());

        harness.handlePermanentChosen(player1, bear.getId());

        Permanent foundEquipment = findPermanent(player1, "Leonin Scimitar");
        assertThat(foundEquipment.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonmatching);
    }

    @Test
    @DisplayName("An Aura enters attached to a chosen creature, including an opponent's creature")
    void auraEntersAttachedToChosenCreature() {
        addCreatureReady(player1, new ArmoredSkyhunter());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Card aura = new HolyStrength();
        Card remainder = new Shock();
        harness.setLibrary(player1, List.of(aura, remainder));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo())
                .isEqualTo(opponentCreature.getId());
        harness.assertNotInGraveyard(player1, "Holy Strength");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainder);
    }

    @Test
    @DisplayName("The controller may decline to put any card onto the battlefield")
    void mayDeclineSelection() {
        addCreatureReady(player1, new ArmoredSkyhunter());
        Card aura = new HolyStrength();
        Card equipment = new LeoninScimitar();
        Card remainder = new Shock();
        harness.setLibrary(player1, List.of(aura, equipment, remainder));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(aura, equipment, remainder);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Equipment can enter without being attached")
    void mayDeclineEquipmentAttachment() {
        addCreatureReady(player1, new ArmoredSkyhunter());
        Card equipment = new LeoninScimitar();
        harness.setLibrary(player1, List.of(equipment));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the top six cards are considered and the remainder goes below untouched cards")
    void looksAtExactlySixCardsAndBottomsTheRest() {
        addCreatureReady(player1, new ArmoredSkyhunter());
        Card equipment = new LeoninScimitar();
        List<Card> rest = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        Card seventh = new HolyStrength();
        List<Card> library = new ArrayList<>();
        library.add(equipment);
        library.addAll(rest);
        library.add(seventh);
        harness.setLibrary(player1, library);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(equipment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6);
        assertThat(deck.getFirst()).isSameAs(seventh);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrderElementsOf(rest);
    }

    @Test
    @DisplayName("Only one Aura or Equipment may be selected")
    void cannotSelectTwoCards() {
        addCreatureReady(player1, new ArmoredSkyhunter());
        Card aura = new HolyStrength();
        Card equipment = new LeoninScimitar();
        harness.setLibrary(player1, List.of(aura, equipment));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(aura.getId(), equipment.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.handleMultipleCardsChosen(player1, List.of());
    }

    @Test
    @DisplayName("When no card matches, the looked-at cards go beneath the untouched library")
    void noEligibleCardsAreBottomedWithoutAChoice() {
        addCreatureReady(player1, new ArmoredSkyhunter());
        List<Card> topSix = List.of(new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        Card seventh = new LeoninScimitar();
        List<Card> library = new ArrayList<>(topSix);
        library.add(seventh);
        harness.setLibrary(player1, library);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(7);
        assertThat(deck.getFirst()).isSameAs(seventh);
        assertThat(deck.subList(1, 7)).containsExactlyInAnyOrderElementsOf(topSix);
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not create a choice or draw a card")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new ArmoredSkyhunter());
        harness.setLibrary(player1, List.of());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Armored Skyhunter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
