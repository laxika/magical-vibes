package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DimirSignet;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LibraryOfLeng;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScytheSpecter.class, DimirSignet.class, Island.class, LibraryOfLeng.class})
class ScytheSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the opponent discard and lose life equal to the discarded card's mana value")
    void combatDamageCausesManaValueLifeLoss() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new DimirSignet()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("A zero-mana-value discard causes no life loss")
    void zeroManaValueDiscardCausesNoLifeLoss() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new Island()));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An opponent with no cards to discard loses no life")
    void emptyHandCausesNoLifeLoss() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Only the chosen card determines life loss, and the controller does not discard")
    void chosenCardDeterminesLifeLoss() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        Card controllerCard = new DimirSignet();
        Card retainedCard = new ScytheSpecter();
        Card discardedCard = new DimirSignet();
        harness.setHand(player1, List.of(controllerCard));
        harness.setHand(player2, List.of(retainedCard, discardedCard));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retainedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discardedCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Only the opponent who discards the greatest mana value loses life")
    void greatestManaValueAmongAllOpponents() {
        Player player3 = addThirdPlayer();
        harness.setHand(player2, List.of(new DimirSignet()));
        harness.setHand(player3, List.of(new ScytheSpecter()));

        startMultiplayerTrigger();
        harness.handleCardChosen(player2, 0);
        harness.assertLife(player2, 16);
        harness.assertLife(player3, 20);
        harness.handleCardChosen(player3, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        harness.assertLife(player3, 14);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player3.getId())).isEmpty();
    }

    @Test
    @DisplayName("All opponents tied for greatest discarded mana value lose that much life")
    void tiedGreatestManaValues() {
        Player player3 = addThirdPlayer();
        harness.setHand(player2, List.of(new DimirSignet()));
        harness.setHand(player3, List.of(new DimirSignet()));

        startMultiplayerTrigger();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player3, 0);

        harness.assertLife(player2, 14);
        harness.assertLife(player3, 18);
    }

    @Test
    @DisplayName("An empty-handed opponent does not interfere with the other opponent's discard")
    void emptyHandInMultiplayer() {
        Player player3 = addThirdPlayer();
        harness.setHand(player2, List.of());
        harness.setHand(player3, List.of(new DimirSignet()));

        startMultiplayerTrigger();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player3.getId());
        harness.handleCardChosen(player3, 0);

        harness.assertLife(player2, 16);
        harness.assertLife(player3, 18);
    }

    @Test
    @DisplayName("Discarded cards stay hidden until every opponent has chosen")
    void discardsWaitForAllOpponentsToChoose() {
        Player player3 = addThirdPlayer();
        Card firstDiscard = new DimirSignet();
        Card secondDiscard = new ScytheSpecter();
        harness.setHand(player2, List.of(firstDiscard));
        harness.setHand(player3, List.of(secondDiscard));

        startMultiplayerTrigger();
        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player3.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(firstDiscard);

        harness.handleCardChosen(player3, 0);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstDiscard);
        assertThat(gd.playerGraveyards.get(player3.getId())).contains(secondDiscard);
    }

    @Test
    @DisplayName("A discard put onto the library without being revealed causes no mana-value life loss")
    void hiddenDiscardHasNoDefinedManaValue() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        Card discardedCard = new DimirSignet();
        harness.addToBattlefield(player2, new LibraryOfLeng());
        harness.setHand(player2, List.of(discardedCard));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(discardedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(discardedCard);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Separate Specter triggers compare only the cards discarded for that trigger")
    void separateTriggersDoNotReuseDiscardManaValues() {
        Permanent first = addCreatureReady(player1, new ScytheSpecter());
        Permanent second = addCreatureReady(player1, new ScytheSpecter());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.setHand(player2, List.of(new ScytheSpecter(), new DimirSignet()));

        harness.resolveCombatDamage();
        harness.assertLife(player2, 12);
        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 4);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    private void startMultiplayerTrigger() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        specter.setAttackTarget(player2.getId());
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private Player addThirdPlayer() {
        UUID playerId = UUID.randomUUID();
        Player opponent = new Player(playerId, "Charlie");
        gd.playerIds.add(playerId);
        gd.orderedPlayerIds.add(playerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(playerId, "Charlie");
        gd.playerDecks.put(playerId, new ArrayList<>());
        gd.playerHands.put(playerId, new ArrayList<>());
        gd.playerBattlefields.put(playerId, new ArrayList<>());
        gd.playerGraveyards.put(playerId, new ArrayList<>());
        gd.playerCommandZones.put(playerId, new ArrayList<>());
        gd.playerManaPools.put(playerId, new ManaPool());
        gd.playerLifeTotals.put(playerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), playerId, "Charlie");
        return opponent;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.passBothPriorities();
    }
}
