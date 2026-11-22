package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HopToIt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarfallInvocation.class, GrizzlyBears.class, HopToIt.class})
class StarfallInvocationTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesWithoutGift() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player2, opposingCreature);

        cast(false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(ownCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(opposingCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void giftReturnsOneCreatureDestroyedIntoYourGraveyard() {
        Card oldGraveyardCreature = new GrizzlyBears();
        Card ownCreature = new GrizzlyBears();
        Card secondOwnCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldGraveyardCreature));
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player1, secondOwnCreature);
        harness.addToBattlefield(player2, opposingCreature);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(true);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).extracting(Card::getId)
                .containsExactly(ownCreature.getId(), secondOwnCreature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);

        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(oldGraveyardCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(opposingCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void giftWithNoCreaturesDoesNotReturnAnOlderGraveyardCard() {
        Card oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        cast(true);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(oldCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningGiftDoesNotDrawACard() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        int handSize = gd.playerHands.get(player2.getId()).size();

        cast(false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsExactly(topCard.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void regeneratedCreatureIsNotEligibleToReturn() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.addRegenerationShields(survivor.getId(), 1);
        Card destroyedCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, destroyedCreature);

        cast(true);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).extracting(Card::getId).containsExactly(destroyedCreature.getId());
        assertThat(survivor.isTapped()).isTrue();
        assertThat(gd.regenerationShieldCount(survivor.getId())).isZero();

        harness.handleMultipleCardsChosen(player1, List.of(destroyedCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(survivor.getCard().getId(), destroyedCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnEligibilityUsesOwnershipRatherThanPreviousController() {
        Card ownedCreature = new GrizzlyBears();
        Permanent ownedButStolen = harness.addToBattlefieldAndReturn(player2, ownedCreature);
        gd.stolenCreatures.put(ownedButStolen.getId(), player1.getId());
        Card opponentOwnedCreature = new GrizzlyBears();
        Permanent borrowedCreature = harness.addToBattlefieldAndReturn(player1, opponentOwnedCreature);
        gd.stolenCreatures.put(borrowedCreature.getId(), player2.getId());

        cast(true);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).extracting(Card::getId).containsExactly(ownedCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownedCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownedCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(opponentOwnedCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void destroyedTokensDoNotOfferAReturnChoice() {
        createRabbits();

        cast(true);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnChoiceExcludesTokensWhenACreatureCardAlsoDies() {
        createRabbits();
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, creature);

        cast(true);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).extracting(Card::getId).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(creature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void createRabbits() {
        harness.setHand(player1, List.of(new HopToIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    private void cast(boolean giftPromised) {
        harness.setHand(player1, List.of(new StarfallInvocation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorceryWithGift(player1, 0, List.of(), giftPromised);
        harness.passBothPriorities();
    }
}
