package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaLiberator.class, GrizzlyBears.class, LeoninScimitar.class})
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
}
