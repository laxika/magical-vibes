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

        PendingInteraction.SpellbookDraftToExileChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftToExileChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(drafted.getId())
                && entry.sourcePermanentId().equals(archive.getId())
                && entry.faceDown());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, null);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(drafted.getId()));
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(drafted.getId()));
    }

    @Test
    void restrictedManaCannotCastStartingDeckCard() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new TheMysticalArchive());
        archive.setSummoningSick(false);
        Card startingDeckCard = new GrizzlyBears();
        harness.setHand(player1, List.of(startingDeckCard));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId())
                .getOutsideStartingDeckSpellOnlyMana(ManaColor.GREEN)).isEqualTo(2);
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
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(outsideStartingDeckCard.getId()));
    }
}
