package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazoretTheFervent.class, Colossapede.class, Island.class})
class HazoretTheFerventTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card deals 2 damage to each opponent")
    void abilityDealsTwoDamageToEachOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Colossapede()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Colossapede");
    }

    @Test
    @DisplayName("Cannot activate the ability with no card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new HazoretTheFervent());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack with two or more cards in hand")
    void cannotAttackWithTwoCardsInHand() {
        addCreatureReady(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Island(), new Island()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with one or fewer cards in hand")
    void canAttackWithOneCardInHand() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Island()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot block with two or more cards in hand")
    void cannotBlockWithTwoCardsInHand() {
        addCreatureReady(player2, new Colossapede());
        addCreatureReady(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Island(), new Island()));

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with an empty hand even while the opponent has multiple cards")
    void canAttackWithEmptyHand() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island(), new Island()));

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 15);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Can block with zero or one card and survives lethal combat damage")
    void canBlockWithAtMostOneCard(int handSize) {
        harness.setLife(player1, 20);
        addCreatureReady(player2, new Colossapede());
        harness.addToBattlefield(player1, new HazoretTheFervent());
        harness.setHand(player1, handSize == 0 ? List.of() : List.of(new Island()));
        harness.setHand(player2, List.of(new Island(), new Island()));

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Hazoret the Fervent");
        harness.assertInGraveyard(player2, "Colossapede");
    }

    @Test
    @DisplayName("A tapped Hazoret can activate with multiple cards, paying the discard before damage")
    void tappedHazoretCanActivateWithMultipleCards() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefieldAndReturn(player2, new HazoretTheFervent()).tap();
        harness.setHand(player2, List.of(new Island(), new Colossapede(), new Island()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Discarding from two cards to one allows Hazoret to attack")
    void discardingEnablesAttack() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("The ability requires red mana, not just three mana")
    void cannotActivateWithoutRedMana() {
        harness.addToBattlefield(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Island");
        harness.assertNotInGraveyard(player1, "Island");
    }
}
