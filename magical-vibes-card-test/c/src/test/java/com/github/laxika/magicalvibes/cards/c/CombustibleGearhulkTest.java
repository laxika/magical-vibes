package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CombustibleGearhulk.class, GrizzlyBears.class, Shock.class, SerraAngel.class, RestInPeace.class})
class CombustibleGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted opponent chooses whether the controller draws")
    void targetedOpponentChoosesMode() {
        setupLibrary(new GrizzlyBears(), new Shock(), new SerraAngel());
        castAndResolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accepting draws three cards for the controller")
    void acceptingDrawsThreeCards() {
        List<Card> cards = List.of(new GrizzlyBears(), new Shock(), new SerraAngel());
        setupLibrary(cards.toArray(Card[]::new));
        castAndResolveEnterTrigger();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining mills three cards and deals their total mana value")
    void decliningMillsAndDealsTotalManaValue() {
        List<Card> cards = List.of(new GrizzlyBears(), new Shock(), new SerraAngel());
        setupLibrary(cards.toArray(Card[]::new));
        castAndResolveEnterTrigger();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting with fewer than three cards causes an empty-library loss")
    void acceptingWithFewerThanThreeCardsLoses() {
        setupLibrary(new GrizzlyBears(), new Shock());
        castAndResolveEnterTrigger();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cards exiled instead of entering the graveyard still contribute to damage")
    void exiledMilledCardsContributeToDamage() {
        harness.addToBattlefield(player2, new RestInPeace());
        List<Card> cards = List.of(new GrizzlyBears(), new Shock(), new SerraAngel());
        setupLibrary(cards.toArray(Card[]::new));
        castAndResolveEnterTrigger();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        cards.forEach(card -> assertThat(gd.findExiledCard(card.getId())).isNotNull());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Declining with two cards mills both and deals only their total mana value")
    void decliningWithShortLibraryDoesNotLose() {
        List<Card> cards = List.of(new GrizzlyBears(), new Shock());
        setupLibrary(cards.toArray(Card[]::new));
        castAndResolveEnterTrigger();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Declining with an empty library deals no damage and does not cause a loss")
    void decliningWithEmptyLibraryDoesNotLose() {
        setupLibrary();
        castAndResolveEnterTrigger();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Declining mills only the top three cards and leaves the fourth in the library")
    void decliningLeavesRemainingLibraryUntouched() {
        List<Card> milled = List.of(new GrizzlyBears(), new Shock(), new SerraAngel());
        Card remaining = new SerraAngel();
        setupLibrary(milled.get(0), milled.get(1), milled.get(2), remaining);
        castAndResolveEnterTrigger();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyElementsOf(milled.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(remaining.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    private void castAndResolveEnterTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CombustibleGearhulk()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    private void setupLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
