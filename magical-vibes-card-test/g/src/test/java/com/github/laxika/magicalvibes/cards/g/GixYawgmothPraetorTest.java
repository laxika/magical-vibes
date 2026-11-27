package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Swamp;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GixYawgmothPraetor.class, GixianInfiltrator.class, GrizzlyBears.class, Swamp.class})
class GixYawgmothPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature you control may be paid for with 1 life to draw")
    void combatDamageMayBePaidForWithLifeToDraw() {
        Permanent attacker = addCreatureReady(player1, new GixianInfiltrator());
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        attacker.setAttacking(true);
        Card drawn = new GixianInfiltrator();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Discarding X cards exiles X cards and plays a land and a spell from them during resolution")
    void discardXExilesXAndPlaysLandAndSpellDuringResolution() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of(new GixianInfiltrator(), new GixianInfiltrator()));
        GrizzlyBears bears = new GrizzlyBears();
        Swamp swamp = new Swamp();
        Card remainder = new GixianInfiltrator();
        harness.setLibrary(player2, List.of(bears, swamp, remainder));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainder);
        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), swamp.getId());

        harness.handleMultipleCardsChosen(player1, List.of(swamp.getId(), bears.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == bears
                && entry.getControllerId().equals(player1.getId()));
        harness.assertOnBattlefield(player1, "Swamp");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Cards not played during resolution stay exiled and can't be played later")
    void unplayedCardsStayExiledWithoutLaterPermission() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of(new GixianInfiltrator()));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(bears);
    }

    @Test
    @DisplayName("A land can't be played this way once a land was already played this turn")
    void landNotPlayableAfterLandPlay() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of(new GixianInfiltrator()));
        Swamp swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(swamp.getId()));

        harness.assertNotOnBattlefield(player1, "Swamp");
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(swamp);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability still resolves after Gix leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of(new GixianInfiltrator()));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == bears
                && entry.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Choosing zero for X requires no discard and exiles no cards")
    void zeroXRequiresNoDiscardAndExilesNoCards() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of());
        Card libraryCard = new GixianInfiltrator();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingAbilityActivation).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Declining the combat damage payment neither pays life nor draws")
    void decliningPaymentDoesNotPayOrDraw() {
        Permanent attacker = addCreatureReady(player1, new GixianInfiltrator());
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        attacker.setAttacking(true);
        Card drawn = new GixianInfiltrator();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Gix's own combat damage triggers the life payment and draw")
    void ownCombatDamageTriggersDraw() {
        Permanent attacker = addCreatureReady(player1, new GixYawgmothPraetor());
        attacker.setAttacking(true);
        Card drawn = new GixianInfiltrator();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Activation on an opponent's turn casts a creature spell but can't play a land")
    void castsCreatureSpellOnOpponentsTurnButNotLand() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of(new GixianInfiltrator(), new GixianInfiltrator()));
        GrizzlyBears bears = new GrizzlyBears();
        Swamp swamp = new Swamp();
        harness.setLibrary(player2, List.of(bears, swamp));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), swamp.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == bears
                && entry.getControllerId().equals(player1.getId()));
        harness.assertNotOnBattlefield(player1, "Swamp");
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(swamp);
    }

    @Test
    @DisplayName("Another player's creature hitting one of your opponents lets that creature's controller pay 1 life to draw")
    void anotherPlayersCreatureHittingYourOpponentLetsItsControllerDraw() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        Permanent attacker = addCreatureReady(player3, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player3, List.of(drawn));
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player1LifeBefore = gd.getLife(player1.getId());

        resolveCombat(player3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player3.getId());
        harness.handleMayAbilityChosen(player3, true);

        assertThat(gd.getLife(player3.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player3.getId())).containsExactly(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore);
    }

    @Test
    @DisplayName("A creature dealing combat damage to you doesn't trigger it")
    void creatureHittingYouDoesNotTrigger() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        String name = "Charlie";
        Player player = new Player(id, name);
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add(name);
        gd.playerIdToName.put(id, name);
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, name);
        return player;
    }
}
