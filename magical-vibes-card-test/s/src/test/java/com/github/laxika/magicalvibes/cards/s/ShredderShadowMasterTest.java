package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShredderShadowMaster.class, JaceBeleren.class, ParallelLives.class, QueenAllenalOfRuadach.class})
class ShredderShadowMasterTest extends BaseCardTest {

    private Player player3;
    private Player player4;

    @Test
    @DisplayName("Attacking a player creates a nonlegendary copy for each other opponent")
    void attackCreatesNonLegendaryCopyForEachOtherOpponent() {
        addThirdPlayer();
        Permanent shredder = addReadyShredder();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        List<Permanent> copies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
        Permanent copy = copies.getFirst();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
        assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class))
                .anyMatch(action -> action.permanentId().equals(copy.getId()));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(shredder).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Combat damage makes the damaged player lose half their life, rounded up")
    void combatDamageHalvesDamagedPlayersLifeRoundedUp() {
        addReadyShredder().setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    void attackingInTwoPlayerGameCreatesNoCopies() {
        addReadyShredder();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class)).isEmpty();
    }

    @Test
    void attackingPlaneswalkerDoesNotCreateCopies() {
        addThirdPlayer();
        addReadyShredder();
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, jace.getId()));
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class)).isEmpty();
    }

    @Test
    void tokenCopyAlsoHalvesThePlayerItDamages() {
        addThirdPlayer();
        addReadyShredder();
        harness.setLife(player2, 21);
        harness.setLife(player3, 20);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 8);
        harness.assertLife(player3, 7);
    }

    @Test
    void lifeLossUsesLifeTotalWhenTriggerResolves() {
        Permanent shredder = addReadyShredder();
        shredder.setAttacking(true);
        shredder.setAttackTarget(player2.getId());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 15);
        harness.setLife(player2, 22);

        resolveAllTriggers();

        harness.assertLife(player2, 11);
    }

    @Test
    void endOfCombatSacrificeWaitsForDelayedTriggerToResolve() {
        addThirdPlayer();
        addReadyShredder();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy);
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
    }

    @Test
    void tokenControlledByOpponentCannotBeSacrificedByOriginalController() {
        addThirdPlayer();
        addReadyShredder();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(copy);
        gd.playerBattlefields.get(player2.getId()).add(copy);
        copy.setAttacking(false);

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(copy);
    }

    private Permanent addReadyShredder() {
        return addCreatureReady(player1, new ShredderShadowMaster());
    }

    @Test
    @DisplayName("With two other opponents, Queen Allenal and Parallel Lives are ordered before the copies enter")
    void asksToOrderTokenReplacementsForSeveralOpponents() {
        attackWithFourPlayersUnderQueenAndParallelLives();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(shredderCopies()).isEmpty();
    }

    @Test
    @DisplayName("Applying Queen Allenal first doubles each opponent's copy and the Soldier, all sacrificed")
    void queenFirstDoublesEachOpponentsCopyAndSoldier() {
        attackWithFourPlayersUnderQueenAndParallelLives();

        chooseReplacement("Queen Allenal of Ruadach");
        resolveAllTriggers();

        assertThat(shredderCopies()).hasSize(4).allSatisfy(copy -> {
            assertThat(copy.isTapped()).isTrue();
            assertThat(copy.isAttacking()).isTrue();
        });
        assertThat(shredderCopies()).extracting(Permanent::getAttackTarget).containsExactlyInAnyOrder(
                player3.getId(), player3.getId(), player4.getId(), player4.getId());
        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(soldier -> {
            assertThat(soldier.isAttacking()).isTrue();
            assertThat(soldier.getAttackTarget()).isEqualTo(player3.getId());
        });
        List<UUID> sacrificed = gd.getDelayedActions(SacrificeAtEndOfCombat.class).stream()
                .map(SacrificeAtEndOfCombat::permanentId).toList();
        assertThat(sacrificed).containsAll(shredderCopies().stream().map(Permanent::getId).toList());
        assertThat(sacrificed).containsAll(findPermanents(player1, "Soldier").stream().map(Permanent::getId).toList());
    }

    @Test
    @DisplayName("Applying Parallel Lives first doubles each opponent's copy and adds one Soldier")
    void parallelLivesFirstAddsOneSoldier() {
        attackWithFourPlayersUnderQueenAndParallelLives();

        chooseReplacement("Parallel Lives");
        resolveAllTriggers();

        assertThat(shredderCopies()).extracting(Permanent::getAttackTarget).containsExactlyInAnyOrder(
                player3.getId(), player3.getId(), player4.getId(), player4.getId());
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    private void attackWithFourPlayersUnderQueenAndParallelLives() {
        addThirdPlayer();
        player4 = addPlayer("Dana", "conn-4");
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        Permanent shredder = addReadyShredder();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(shredder)));
            harness.passBothPriorities();
        });
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.handleListChoice(player1,
                choice.options().stream().filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow()));
    }

    private List<Permanent> shredderCopies() {
        return findPermanents(player1, "Shredder, Shadow Master").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void addThirdPlayer() {
        player3 = addPlayer("Charlie", "conn-3");
    }

    private Player addPlayer(String name, String connectionId) {
        UUID playerId = UUID.randomUUID();
        Player player = new Player(playerId, name);
        gd.playerIds.add(playerId);
        gd.orderedPlayerIds.add(playerId);
        gd.playerNames.add(name);
        gd.playerIdToName.put(playerId, name);
        gd.playerDecks.put(playerId, new ArrayList<>());
        gd.playerHands.put(playerId, new ArrayList<>());
        gd.playerBattlefields.put(playerId, new ArrayList<>());
        gd.playerGraveyards.put(playerId, new ArrayList<>());
        gd.playerCommandZones.put(playerId, new ArrayList<>());
        gd.playerManaPools.put(playerId, new ManaPool());
        gd.playerLifeTotals.put(playerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection(connectionId), playerId, name);
        return player;
    }
}
