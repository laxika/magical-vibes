package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheAncientOne.class, GrizzlyBears.class, Shock.class, HillGiant.class, Forest.class})
class TheAncientOneTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack with fewer than eight permanent cards in its controller's graveyard")
    void cannotAttackWithoutEightPermanentCards() {
        List<Card> graveyard = new ArrayList<>(permanentCards(7));
        graveyard.add(new Shock());
        harness.setGraveyard(player1, graveyard);
        addCreatureReady(player1, new TheAncientOne());
        harness.setGraveyard(player2, permanentCards(8));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack and block with eight permanent cards in its controller's graveyard")
    void canAttackAndBlockWithEightPermanentCards() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, permanentCards(8));
        Permanent attacker = addCreatureReady(player1, new TheAncientOne());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);

        harness.setGraveyard(player2, permanentCards(8));
        Permanent blocker = addCreatureReady(player2, new TheAncientOne());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Draws, discards, then mills by the discarded card's mana value")
    void drawsDiscardsAndMillsByDiscardedManaValue() {
        addCreatureReady(player1, new TheAncientOne());
        HillGiant discarded = new HillGiant();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot block with only seven permanent cards, even with eight cards total")
    void cannotBlockBelowPermanentThreshold() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new TheAncientOne());
        List<Card> graveyard = new ArrayList<>(permanentCards(7));
        graveyard.add(new Shock());
        harness.setGraveyard(player2, graveyard);
        harness.setGraveyard(player1, permanentCards(8));
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding a drawn land still triggers but mills zero cards")
    void drawnLandCanBeDiscardedToMillZero() {
        addCreatureReady(player1, new TheAncientOne());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A second activation's discard cannot change the first mill trigger's amount")
    void millAmountBelongsToItsOwnDiscard() {
        addCreatureReady(player1, new TheAncientOne());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    private List<Card> permanentCards(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new GrizzlyBears())
                .toList();
    }

}
