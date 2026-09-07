package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
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

@CardUsed({GilgameshMasterAtArms.class, LeoninScimitar.class, StriderHarness.class, Shock.class})
class GilgameshMasterAtArmsTest extends BaseCardTest {

    @Test
    @DisplayName("When Gilgamesh enters, it puts selected Equipment onto the battlefield")
    void entersAndPutsEquipmentOntoBattlefield() {
        Card scimitar = new LeoninScimitar();
        Card harnessCard = new StriderHarness();
        Card shock = new Shock();
        harness.setHand(player1, List.of(new GilgameshMasterAtArms()));
        harness.setLibrary(player1, List.of(scimitar, shock, harnessCard));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(scimitar.getId(), harnessCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(scimitar.getId(), harnessCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Strider Harness");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("When Gilgamesh attacks, it may attach one found Equipment to a Samurai")
    void attacksAndAttachesOneEquipmentToSamurai() {
        Permanent gilgamesh = addCreatureReady(player1, new GilgameshMasterAtArms());
        Card scimitar = new LeoninScimitar();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(scimitar, shock));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(scimitar.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent equipment = findPermanent(player1, "Leonin Scimitar");
        assertThat(equipment.getAttachedTo()).isEqualTo(gilgamesh.getId());
    }
}
