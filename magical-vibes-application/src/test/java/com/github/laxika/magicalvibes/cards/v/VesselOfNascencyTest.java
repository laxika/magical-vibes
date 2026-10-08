package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AimHigh;
import com.github.laxika.magicalvibes.cards.a.AngelicPurge;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JaceUnravelerOfSecrets;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.s.SolitaryHunter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VesselOfNascency.class, MagnifyingGlass.class, SolitaryHunter.class,
        DeadWeight.class, Forest.class, JaceUnravelerOfSecrets.class, AimHigh.class, AngelicPurge.class})
class VesselOfNascencyTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself, offers permanent cards, and puts the rest into the graveyard")
    void sacrificesItselfOffersPermanentCardsAndBinsRest() {
        Card artifact = new MagnifyingGlass();
        Card creature = new SolitaryHunter();
        Card enchantment = new DeadWeight();
        Card land = new Forest();
        harness.addToBattlefield(player1, new VesselOfNascency());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(artifact, creature, enchantment, land));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Vessel of Nascency");
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(artifact, creature, enchantment, land);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(artifact, enchantment, land)
                .doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Offers planeswalker cards but not instants, and may decline the card")
    void offersPlaneswalkersButNotInstantsAndMayDecline() {
        Card planeswalker = new JaceUnravelerOfSecrets();
        Card instant = new AimHigh();
        harness.addToBattlefield(player1, new VesselOfNascency());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(planeswalker, instant));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(planeswalker);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(planeswalker, instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsAllCardsIntoGraveyardWhenNoneAreEligible() {
        Card instant = new AimHigh();
        Card sorcery = new AngelicPurge();
        harness.addToBattlefield(player1, new VesselOfNascency());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(instant, sorcery));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant, sorcery);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void revealsOnlyFourCardsAndLeavesRemainingLibraryInOrder() {
        Card artifact = new MagnifyingGlass();
        Card enchantment = new DeadWeight();
        Card instant = new AimHigh();
        Card sorcery = new AngelicPurge();
        Card fifth = new Forest();
        Card sixth = new SolitaryHunter();
        harness.addToBattlefield(player1, new VesselOfNascency());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(artifact, enchantment, instant, sorcery, fifth, sixth));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(artifact, enchantment);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(artifact, instant, sorcery).doesNotContain(enchantment, fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, sixth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithAnEmptyLibraryAfterPayingTheSacrificeCost() {
        harness.addToBattlefield(player1, new VesselOfNascency());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Vessel of Nascency");
        harness.assertInGraveyard(player1, "Vessel of Nascency");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
