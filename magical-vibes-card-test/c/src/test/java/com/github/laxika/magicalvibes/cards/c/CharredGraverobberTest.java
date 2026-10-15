package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharredGraverobber.class, DeathcultRogue.class, GrizzlyBears.class, ChangelingOutcast.class})
class CharredGraverobberTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target outlaw card from the graveyard to hand")
    void returnsTargetOutlawToHand() {
        Card outlaw = new DeathcultRogue();
        harness.setGraveyard(player1, List.of(outlaw, new GrizzlyBears()));
        harness.setHand(player1, List.of(new CharredGraverobber()));
        addNormalMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(outlaw.getId());
        harness.handleMultipleCardsChosen(player1, List.of(outlaw.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deathcult Rogue");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Escape exiles four other cards and enters with a +1/+1 counter")
    void escapesWithCounter() {
        CharredGraverobber robber = new CharredGraverobber();
        List<Card> exiledCards = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, List.of(robber, exiledCards.get(0), exiledCards.get(1),
                exiledCards.get(2), exiledCards.get(3)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        resolveAllTriggers();

        Permanent escaped = findPermanent(player1, "Charred Graverobber");
        assertThat(escaped.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(exiledCards);
    }

    @Test
    @DisplayName("Cannot escape without four other graveyard cards")
    void escapeRequiresFourOtherCards() {
        harness.setGraveyard(player1, List.of(new CharredGraverobber(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 4");
    }

    @Test
    void normalCastHasNoCounterAndCannotReturnOpponentsOutlaw() {
        Card opposingOutlaw = new CharredGraverobber();
        harness.setGraveyard(player2, List.of(opposingOutlaw));
        harness.castFromHand(player1, new CharredGraverobber(), "{2}{B}");

        resolveAllTriggers();

        assertThat(findPermanent(player1, "Charred Graverobber")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingOutlaw);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsChangelingAsOutlaw() {
        Card outlaw = new ChangelingOutcast();
        harness.setGraveyard(player1, List.of(outlaw));
        harness.castFromHand(player1, new CharredGraverobber(), "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(outlaw.getId());
        harness.handleMultipleCardsChosen(player1, List.of(outlaw.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Changeling Outcast");
        harness.assertNotInGraveyard(player1, "Changeling Outcast");
    }

    @Test
    void escapeStillReturnsAnOutlawNotExiledForItsCost() {
        harness.setHand(player1, List.of());
        Card robber = new CharredGraverobber();
        Card retainedOutlaw = new CharredGraverobber();
        List<Card> fodder = List.of(new ChangelingOutcast(), new ChangelingOutcast(),
                new ChangelingOutcast(), new ChangelingOutcast());
        harness.setGraveyard(player1, List.of(robber, retainedOutlaw,
                fodder.get(0), fodder.get(1), fodder.get(2), fodder.get(3)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(2, 3, 4, 5));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Charred Graverobber")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(retainedOutlaw.getId());
        harness.handleMultipleCardsChosen(player1, List.of(retainedOutlaw.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedOutlaw);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(fodder);
    }

    @Test
    void targetRemovedBeforeResolutionIsNotReturned() {
        Card outlaw = new CharredGraverobber();
        harness.setGraveyard(player1, List.of(outlaw));
        harness.castFromHand(player1, new CharredGraverobber(), "{2}{B}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(outlaw.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(outlaw));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(outlaw);
        harness.assertOnBattlefield(player1, "Charred Graverobber");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
