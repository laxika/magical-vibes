package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfNissa.class, Forest.class, GrizzlyBears.class, JaceBeleren.class, Shock.class})
class OathOfNissaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers creature, land, and planeswalker cards among the top three")
    void etbOffersCreatureLandAndPlaneswalker() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card planeswalker = new JaceBeleren();
        harness.setLibrary(player1, List.of(creature, land, planeswalker));

        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(creature.getId(), land.getId(), planeswalker.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB excludes cards that are not creatures, lands, or planeswalkers")
    void etbExcludesOtherCardTypes() {
        Card planeswalker = new JaceBeleren();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(planeswalker, land, instant));

        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(planeswalker.getId(), land.getId());
        assertThat(choice.validCardIds()).doesNotContain(instant.getId());
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and bottoms the rest")
    void choosingCardPutsItIntoHandAndBottomsRest() {
        Card planeswalker = new JaceBeleren();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(planeswalker, land, instant));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(planeswalker.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(land, instant);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining leaves all three cards on the bottom of the library")
    void decliningBottomsAllCards() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(creature, land, instant));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature, land, instant);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(creature, land, instant);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant, land, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Allows a planeswalker to be cast with only off-color mana")
    void castsPlaneswalkerWithOffColorMana() {
        harness.addToBattlefield(player1, new OathOfNissa());
        JaceBeleren planeswalker = new JaceBeleren();
        harness.setHand(player1, List.of(planeswalker));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player1.getId(), planeswalker,
                        gd.playerManaPools.get(player1.getId()), 0)).isTrue();

        harness.castPlaneswalker(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jace Beleren");
    }

    @Test
    @DisplayName("Does not allow off-color mana to cast a planeswalker without Oath of Nissa")
    void doesNotAllowOffColorPlaneswalkerWithoutOath() {
        JaceBeleren planeswalker = new JaceBeleren();
        harness.setHand(player1, List.of(planeswalker));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player1.getId(), planeswalker,
                        gd.playerManaPools.get(player1.getId()), 0)).isFalse();
    }

    @Test
    @DisplayName("With no eligible cards, the controller chooses their order on the bottom")
    void noEligibleCardsCanBeOrderedOnBottom() {
        Card first = new Shock();
        Card second = new Shock();
        Card third = new Shock();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, untouched));

        castAndResolveEtb();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library finishes without a choice")
    void emptyLibraryFinishesNormally() {
        harness.setLibrary(player1, List.of());

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Oath of Nissa");
    }

    @Test
    @DisplayName("A library with one eligible card can put it into hand")
    void singleCardLibraryCanBeChosen() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Colorless mana can pay the colored cost of a planeswalker")
    void castsPlaneswalkerWithColorlessMana() {
        harness.addToBattlefield(player1, new OathOfNissa());
        harness.setHand(player1, List.of(new JaceBeleren()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Jace Beleren");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The mana permission does not apply to creature spells")
    void doesNotAllowOffColorCreatureMana() {
        harness.addToBattlefield(player1, new OathOfNissa());
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player1.getId(), creature,
                        gd.playerManaPools.get(player1.getId()), 0)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Oath does not grant the mana permission")
    void opponentsOathDoesNotGrantPermission() {
        harness.addToBattlefield(player2, new OathOfNissa());
        Card planeswalker = new JaceBeleren();
        harness.setHand(player1, List.of(planeswalker));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player1.getId(), planeswalker,
                        gd.playerManaPools.get(player1.getId()), 0)).isFalse();
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new OathOfNissa()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
    }

}
