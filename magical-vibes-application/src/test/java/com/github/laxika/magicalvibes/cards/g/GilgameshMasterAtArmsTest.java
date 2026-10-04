package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BusterSword;
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

@CardUsed({GilgameshMasterAtArms.class, LeoninScimitar.class, StriderHarness.class, Shock.class,
        BusterSword.class})
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
        resolveAllTriggers();

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

    @Test
    void mayDeclineAllEquipmentWithoutCreatingAttachmentAbility() {
        Card equipment = new BusterSword();
        Card other = new GilgameshMasterAtArms();
        harness.setLibrary(player1, List.of(equipment, other));
        harness.enterBattlefieldAndReturn(player1, new GilgameshMasterAtArms());
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Buster Sword");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equipment, other);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void looksOnlyAtTopSixAndLeavesUnlookedCardsOnTop() {
        Card first = new BusterSword();
        Card second = new BusterSword();
        Card third = new BusterSword();
        Card fourth = new BusterSword();
        Card fifth = new BusterSword();
        Card sixth = new BusterSword();
        Card seventh = new BusterSword();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth, seventh));
        harness.enterBattlefieldAndReturn(player1, new GilgameshMasterAtArms());
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), third.getId(),
                fourth.getId(), fifth.getId(), sixth.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrder(second, third, fourth, fifth, sixth);
        assertThat(findPermanent(player1, "Buster Sword").getAttachedTo()).isNull();
    }

    @Test
    void attachesOnlyChosenNewEquipmentAndLeavesExistingEquipmentAlone() {
        Permanent oldEquipment = harness.addToBattlefieldAndReturn(player1, new BusterSword());
        Card first = new BusterSword();
        Card second = new BusterSword();
        harness.setLibrary(player1, List.of(first, second));
        Permanent gilgamesh = harness.enterBattlefieldAndReturn(player1, new GilgameshMasterAtArms());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> swords = findPermanents(player1, "Buster Sword");
        Permanent chosen = swords.stream().filter(p -> p.getCard().getId().equals(second.getId()))
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(chosen.getAttachedTo()).isEqualTo(gilgamesh.getId());
        assertThat(oldEquipment.getAttachedTo()).isNull();
        assertThat(swords.stream().filter(p -> !p.getId().equals(chosen.getId())))
                .allSatisfy(p -> assertThat(p.getAttachedTo()).isNull());
    }

    @Test
    void noEquipmentPutsLookedCardsBelowUnlookedCardsWithoutChoices() {
        List<Card> lookedCards = List.of(new GilgameshMasterAtArms(), new GilgameshMasterAtArms(),
                new GilgameshMasterAtArms(), new GilgameshMasterAtArms(),
                new GilgameshMasterAtArms(), new GilgameshMasterAtArms());
        Card unlooked = new BusterSword();
        java.util.ArrayList<Card> library = new java.util.ArrayList<>(lookedCards);
        library.add(unlooked);
        harness.setLibrary(player1, library);
        harness.enterBattlefieldAndReturn(player1, new GilgameshMasterAtArms());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unlooked);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(lookedCards);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Buster Sword");
    }

    @Test
    void emptyLibraryCreatesNoChoicesOrAttachmentAbility() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new GilgameshMasterAtArms());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
