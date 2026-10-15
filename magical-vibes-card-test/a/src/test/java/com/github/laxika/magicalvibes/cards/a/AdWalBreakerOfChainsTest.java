package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EzioAuditoreDaFirenze;
import com.github.laxika.magicalvibes.cards.h.HiddenBlade;
import com.github.laxika.magicalvibes.cards.j.Jackdaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdWalBreakerOfChains.class, EzioAuditoreDaFirenze.class, HiddenBlade.class, Jackdaw.class})
class AdWalBreakerOfChainsTest extends BaseCardTest {

    @Test
    void entersAndPutsAnAssassinFromTheTopSixIntoHand() {
        Card assassin = card("Top Assassin", CardType.CREATURE, CardSubtype.ASSASSIN);
        Card pirate = card("Top Pirate", CardType.CREATURE, CardSubtype.PIRATE);
        Card firstFiller = card("First Filler", CardType.CREATURE, CardSubtype.HUMAN);
        Card secondFiller = card("Second Filler", CardType.CREATURE, CardSubtype.HUMAN);
        Card thirdFiller = card("Third Filler", CardType.CREATURE, CardSubtype.HUMAN);
        Card fourthFiller = card("Fourth Filler", CardType.CREATURE, CardSubtype.HUMAN);
        harness.setLibrary(player1, List.of(firstFiller, assassin, pirate, secondFiller,
                thirdFiller, fourthFiller));
        harness.setHand(player1, List.of(new AdWalBreakerOfChains()));
        addManaForAdwale();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(assassin.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(assassin);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstFiller, pirate, secondFiller, thirdFiller, fourthFiller);
    }

    @Test
    void vehicleCombatDamageMayReturnAdwaleFromGraveyard() {
        Card adwale = new AdWalBreakerOfChains();
        gd.playerGraveyards.get(player1.getId()).add(adwale);
        Permanent vehicle = addCreatureReady(player1, card("Vehicle", CardType.CREATURE, CardSubtype.VEHICLE));
        vehicle.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(adwale);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(adwale);
    }

    @Test
    void nonVehicleCombatDamageDoesNotTriggerTheGraveyardAbility() {
        Card adwale = new AdWalBreakerOfChains();
        gd.playerGraveyards.get(player1.getId()).add(adwale);
        Permanent creature = addCreatureReady(player1, card("Creature", CardType.CREATURE, CardSubtype.HUMAN));
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(adwale);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(adwale);
    }

    @Test
    void canChooseAVehicleInsteadOfAnAssassin() {
        Card vehicle = new Jackdaw();
        Card assassin = new EzioAuditoreDaFirenze();
        Card equipment = new HiddenBlade();
        harness.setLibrary(player1, List.of(vehicle, assassin, equipment));

        enterAdwale();
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(vehicle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(assassin, equipment);
    }

    @Test
    void takesTheOnlyEligibleCardFromAShortLibrary() {
        Card assassin = new EzioAuditoreDaFirenze();
        Card equipment = new HiddenBlade();
        harness.setLibrary(player1, List.of(equipment, assassin));

        enterAdwale();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(assassin);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotChooseAnEligibleCardBelowTheTopSix() {
        List<Card> topSix = List.of(new HiddenBlade(), new HiddenBlade(), new HiddenBlade(),
                new HiddenBlade(), new HiddenBlade(), new HiddenBlade());
        Card seventh = new Jackdaw();
        List<Card> library = new ArrayList<>(topSix);
        library.add(seventh);
        harness.setLibrary(player1, library);

        enterAdwale();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(topSix);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void enteringWithAnEmptyLibraryDoesNotAskForAChoice() {
        harness.setLibrary(player1, List.of());

        enterAdwale();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclineTheReturnAfterACrewedVehicleDealsCombatDamage() {
        Card adwale = new AdWalBreakerOfChains();
        harness.setGraveyard(player1, List.of(adwale));
        readyJackdaw(player1);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(adwale);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(adwale);
    }

    @Test
    void anOpponentsVehicleDoesNotReturnAdwale() {
        Card adwale = new AdWalBreakerOfChains();
        harness.setGraveyard(player1, List.of(adwale));
        readyJackdaw(player2);

        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(adwale);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(adwale);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void readyJackdaw(Player controller) {
        Permanent vehicle = addCreatureReady(controller, new Jackdaw());
        addCreatureReady(controller, new EzioAuditoreDaFirenze());
        harness.activateAbility(controller, 0, null, null);
        harness.passBothPriorities();
        vehicle.setAttacking(true);
    }

    private void enterAdwale() {
        harness.setHand(player1, List.of(new AdWalBreakerOfChains()));
        addManaForAdwale();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForAdwale() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private static Card card(String name, CardType type, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtype));
        card.setAdditionalTypes(subtype == CardSubtype.VEHICLE ? Set.of(CardType.ARTIFACT) : Set.of());
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
