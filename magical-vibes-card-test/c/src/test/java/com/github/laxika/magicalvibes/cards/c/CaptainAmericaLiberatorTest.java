package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaLiberator.class, GrizzlyBears.class, LeoninScimitar.class,
        LoxodonWarhammer.class, Condemn.class})
class CaptainAmericaLiberatorTest extends BaseCardTest {

    @Test
    @DisplayName("The enter ability may search for an Equipment with mana value 3 or less")
    void enterAbilitySearchesForEligibleEquipment() {
        Card eligibleEquipment = new LeoninScimitar();
        Card expensiveEquipment = equipment("Expensive Equipment", "{4}");
        Card nonEquipment = new GrizzlyBears();
        harness.setLibrary(player1, List.of(eligibleEquipment, expensiveEquipment, nonEquipment));
        harness.setHand(player1, List.of(new CaptainAmericaLiberator()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(eligibleEquipment.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eligibleEquipment.getId()));
    }

    @Test
    @DisplayName("Attacking creates one Soldier for each attached Equipment")
    void attackCreatesSoldiersForAttachedEquipment() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaLiberator());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        firstEquipment.setAttachedTo(captain.getId());
        secondEquipment.setAttachedTo(captain.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    @DisplayName("Unattached Equipment does not create Soldiers")
    void attackWithNoAttachedEquipmentCreatesNoSoldiers() {
        addCreatureReady(player1, new CaptainAmericaLiberator());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private Card equipment(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        card.setManaCost(manaCost);
        return card;
    }

    @Test
    @DisplayName("The search can be declined without changing the library")
    void enterAbilityCanBeDeclined() {
        Card equipment = new LeoninScimitar();
        Card otherCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(equipment, otherCard));
        harness.setHand(player1, List.of(new CaptainAmericaLiberator()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment, otherCard);
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Equipment with mana value exactly three enters untapped and unattached")
    void searchIncludesManaValueThree() {
        Card equipment = new LoxodonWarhammer();
        harness.setLibrary(player1, List.of(equipment));
        harness.setHand(player1, List.of(new CaptainAmericaLiberator()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent found = findPermanent(player1, "Loxodon Warhammer");
        assertThat(found.isTapped()).isFalse();
        assertThat(found.isAttached()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(equipment);
    }

    @Test
    @DisplayName("A restricted Equipment search may fail to find an eligible card")
    void searchMayFailToFind() {
        Card equipment = new LeoninScimitar();
        harness.setLibrary(player1, List.of(equipment));
        harness.setHand(player1, List.of(new CaptainAmericaLiberator()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Equipment controlled by an opponent counts, but Equipment on another creature does not")
    void attackCountsOnlyEquipmentAttachedToCaptainRegardlessOfController() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaLiberator());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentsEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent otherEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        opponentsEquipment.setAttachedTo(captain.getId());
        otherEquipment.setAttachedTo(otherCreature.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger counts Equipment as it resolves")
    void attackUsesCurrentEquipmentCount() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaLiberator());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(captain.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, equipment);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger uses last known attachments when Captain America leaves")
    void attackUsesLastKnownEquipmentCountAfterCaptainLeaves() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaLiberator());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(captain.getId());
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.castAndResolveInstant(player2, 0, captain.getId());
            harness.assertNotOnBattlefield(player1, "Captain America, Liberator");
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }
}
