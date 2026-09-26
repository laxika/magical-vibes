package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AdWalBreakerOfChains.class)
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
        Permanent vehicle = new Permanent(card("Vehicle", CardType.CREATURE, CardSubtype.VEHICLE));
        vehicle.setSummoningSick(false);
        vehicle.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(vehicle);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(adwale);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(adwale);
    }

    @Test
    void nonVehicleCombatDamageDoesNotTriggerTheGraveyardAbility() {
        Card adwale = new AdWalBreakerOfChains();
        gd.playerGraveyards.get(player1.getId()).add(adwale);
        Permanent creature = new Permanent(card("Creature", CardType.CREATURE, CardSubtype.HUMAN));
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(adwale);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(adwale);
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
