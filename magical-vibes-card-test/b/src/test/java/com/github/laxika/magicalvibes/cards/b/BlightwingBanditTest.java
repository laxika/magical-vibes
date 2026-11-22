package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightwingBandit.class, Divination.class, Island.class, Shock.class})
class BlightwingBanditTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may look at the card exiled face down")
    void controllerMayLookAtTheExiledCard() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new Island()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.mayLookAtExiledCard(topCard.getId(), player1.getId())).isTrue();
        assertThat(gd.mayLookAtExiledCard(topCard.getId(), player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The first spell during an opponent's turn exiles the top card of that player's library face down")
    void firstSpellDuringOpponentsTurnExilesTopCardFaceDown() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        Card topCard = new Divination();
        Card ownTopCard = new Island();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(topCard, new Island()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(topCard.getId(), player1.getId());
        assertThat(entry.exilerId()).isEqualTo(player1.getId());
        assertThat(gd.exilePlayCardsWithDuration(Duration.UNTIL_END_OF_TURN)).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeCardIds()).contains(topCard.getId());
    }

    @Test
    @DisplayName("Only the first spell during an opponent's turn triggers")
    void onlyFirstSpellDuringOpponentsTurnTriggers() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        int librarySizeAfterFirstSpell = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeAfterFirstSpell);
    }

    @Test
    @DisplayName("The ability does not trigger during its controller's turn")
    void doesNotTriggerDuringOwnTurn() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        Card topCard = new Island();
        Card opponentTopCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
    }

    @Test
    @DisplayName("The controller can cast the exiled spell using mana of any type")
    void castsExiledSpellWithAnyManaType() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        Card topCard = new Divination();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(topCard, new Island()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }
}
