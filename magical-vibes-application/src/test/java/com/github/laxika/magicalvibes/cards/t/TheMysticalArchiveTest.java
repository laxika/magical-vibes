package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMysticalArchive.class, GrizzlyBears.class})
class TheMysticalArchiveTest extends BaseCardTest {

    @Test
    void draftsFaceDownCardWithSourceAndReturnsItToHand() {
        Permanent archive = harness.enterBattlefieldAndReturn(player1, new TheMysticalArchive());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftToExileChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftToExileChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(drafted.getId())
                && entry.sourcePermanentId().equals(archive.getId())
                && entry.faceDown());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(drafted.getId()));
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(drafted.getId()));
    }

    @Test
    void restrictedManaCannotCastStartingDeckCard() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new TheMysticalArchive());
        archive.setSummoningSick(false);
        Card startingDeckCard = new GrizzlyBears();
        harness.setHand(player1, List.of(startingDeckCard));
        assertThat(gd.startingDeckCardIds.get(player1.getId())).contains(startingDeckCard.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId())
                .getOutsideStartingDeckSpellOnlyMana(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCanCastCardOutsideStartingDeck() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new TheMysticalArchive());
        archive.setSummoningSick(false);
        Card outsideStartingDeckCard = new GrizzlyBears();
        outsideStartingDeckCard.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(outsideStartingDeckCard));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(outsideStartingDeckCard.getId()));
    }

    @Test
    void draftingIsAnAsEntersChoiceRatherThanAStackTrigger() {
        harness.setHand(player1, List.of(new TheMysticalArchive()));
        harness.playLand(player1, 0);

        PendingInteraction.SpellbookDraftToExileChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftToExileChoice.class);
        assertThat(choice).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(choice.cards()).hasSize(3);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));
    }

    @Test
    void newControllerReturnsExiledCardToItsOwner() {
        Permanent archive = harness.addToBattlefieldAndReturn(player2, new TheMysticalArchive());
        Card exiled = new GrizzlyBears();
        exiled.setOwnerId(player1.getId());
        gd.addToExile(player1.getId(), exiled, archive.getId(), true);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 2, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiled);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(exiled);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(exiled.getId()));
    }

    @Test
    void colorlessManaAbilityResolvesImmediatelyAndTapsLand() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new TheMysticalArchive());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(archive.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaCanBeTwoDifferentColorsAndCannotPayForAnAbility() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new TheMysticalArchive());
        harness.addToBattlefield(player1, new TheMysticalArchive());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(archive.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getOutsideStartingDeckSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getOutsideStartingDeckSpellOnlyMana(ManaColor.RED)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void retrievalDoesNotTakeCardsExiledWithAnotherArchive() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new TheMysticalArchive());
        Permanent otherArchive = harness.addToBattlefieldAndReturn(player1, new TheMysticalArchive());
        Card exiled = new GrizzlyBears();
        exiled.setOwnerId(player1.getId());
        gd.addToExile(player1.getId(), exiled, otherArchive.getId(), true);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null, null);
        resolveAllTriggers();

        assertThat(archive.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(exiled);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(exiled.getId()));
    }

    @Test
    void retrievalStillResolvesAfterSourceLeavesBattlefield() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new TheMysticalArchive());
        Card exiled = new GrizzlyBears();
        exiled.setOwnerId(player1.getId());
        gd.addToExile(player1.getId(), exiled, archive.getId(), true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(archive);
        gd.playerGraveyards.get(player1.getId()).add(archive.getCard());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiled);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(exiled.getId()));
    }
}
