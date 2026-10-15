package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KroxaTitanOfDeathsHunger.class, NyxbornCourser.class, Island.class})
class KroxaTitanOfDeathsHungerTest extends BaseCardTest {

    @Test
    void castFromHandSacrificesKroxa() {
        castFromHandWithOpponentHand(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof KroxaTitanOfDeathsHunger);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof KroxaTitanOfDeathsHunger);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void opponentLosesLifeAfterDiscardingALand() {
        castFromHandWithOpponentHand(List.of(new Island()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void opponentDoesNotLoseLifeAfterDiscardingANonland() {
        castFromHandWithOpponentHand(List.of(new NyxbornCourser()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void escapedKroxaStaysOnTheBattlefield() {
        KroxaTitanOfDeathsHunger kroxa = new KroxaTitanOfDeathsHunger();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(kroxa);
        graveyard.addAll(IntStream.range(0, 5).mapToObj(ignored -> new NyxbornCourser()).toList());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromGraveyard(player1, 0, IntStream.rangeClosed(1, 5).boxed().toList());
        resolveAllTriggers();

        Permanent escapedKroxa = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(escapedKroxa.getCard()).isInstanceOf(KroxaTitanOfDeathsHunger.class);
        assertThat(escapedKroxa.isEscaped()).isTrue();
    }

    @Test
    void attackingKroxaMakesOpponentDiscard() {
        addCreatureReady(player1, new KroxaTitanOfDeathsHunger());
        harness.setHand(player2, new ArrayList<>(List.of(new NyxbornCourser())));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void entersWithTwoSeparateTriggeredAbilities() {
        harness.setHand(player1, List.of(new KroxaTitanOfDeathsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof KroxaTitanOfDeathsHunger);
    }

    @Test
    void opponentChoosesWhichCardToDiscard() {
        NyxbornCourser creature = new NyxbornCourser();
        harness.setHand(player1, List.of(new KroxaTitanOfDeathsHunger()));
        harness.setHand(player2, List.of(new Island(), creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1)
                .allMatch(card -> card instanceof Island);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void escapePaysFiveOtherCardsAndStillTriggersLifeLoss() {
        prepareEscapeGraveyard(5);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(5)
                .allMatch(card -> card instanceof NyxbornCourser);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void cannotEscapeWithOnlyFourOtherCards() {
        prepareEscapeGraveyard(4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotExileKroxaToPayItsOwnEscapeCost() {
        prepareEscapeGraveyard(5);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotEscapeByPayingOnlyItsNormalManaCost() {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new KroxaTitanOfDeathsHunger());
        graveyard.addAll(IntStream.range(0, 5).mapToObj(ignored -> new NyxbornCourser()).toList());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void attackCausesLifeLossWhenOpponentHasNoCards() {
        addCreatureReady(player1, new KroxaTitanOfDeathsHunger());
        harness.setHand(player2, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(11);
    }

    @Test
    void attackCausesLifeLossWhenOpponentDiscardsLand() {
        addCreatureReady(player1, new KroxaTitanOfDeathsHunger());
        harness.setHand(player2, List.of(new Island()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(11);
    }

    @Test
    void opponentsChooseBeforeAnyChosenCardIsDiscarded() {
        Player thirdPlayer = addOpponent();
        NyxbornCourser firstChoice = new NyxbornCourser();
        Island secondChoice = new Island();
        harness.setHand(player1, List.of(new KroxaTitanOfDeathsHunger()));
        harness.setHand(player2, List.of(firstChoice));
        harness.setHand(thirdPlayer, List.of(secondChoice));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        while (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.clearPriorityPassed();
            gs.passPriority(gd, player1);
            gs.passPriority(gd, player2);
            gs.passPriority(gd, thirdPlayer);
        }
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(thirdPlayer.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(firstChoice);
        assertThat(gd.getLife(thirdPlayer.getId())).isEqualTo(20);

        harness.handleCardChosen(thirdPlayer, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstChoice);
        assertThat(gd.playerGraveyards.get(thirdPlayer.getId())).contains(secondChoice);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(thirdPlayer.getId())).isEqualTo(17);
    }

    private void prepareEscapeGraveyard(int otherCardCount) {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new KroxaTitanOfDeathsHunger());
        graveyard.addAll(IntStream.range(0, otherCardCount).mapToObj(ignored -> new NyxbornCourser()).toList());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private Player addOpponent() {
        Player opponent = new Player(UUID.randomUUID(), "Charlie");
        UUID id = opponent.getId();
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        return opponent;
    }

    private void castFromHandWithOpponentHand(List<Card> opponentHand) {
        harness.setHand(player1, List.of(new KroxaTitanOfDeathsHunger()));
        harness.setHand(player2, new ArrayList<>(opponentHand));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player2, 0);
        }
        resolveAllTriggers();
    }
}
