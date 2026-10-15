package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FleshbagMarauder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMasterMultiplied.class, FleshbagMarauder.class})
class TheMasterMultipliedTest extends BaseCardTest {

    @Test
    @DisplayName("Two nontoken Masters still require a legend-rule choice")
    void nontokenCopiesAreSubjectToTheLegendRule() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TheMasterMultiplied());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TheMasterMultiplied());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.LegendRule.class);
        harness.handlePermanentChosen(player1, first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second.getCard());
    }

    @Test
    void creatureTokenCopiesSurviveAlongsideOneNontokenMaster() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterMultiplied());
        Permanent firstToken = addMasterToken(player1);
        Permanent secondToken = addMasterToken(player1);

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(master, firstToken, secondToken);
    }

    @Test
    @DisplayName("Myriad tokens remain when a controlled triggered ability would exile them")
    void myriadTokenIsNotExiledAtEndOfCombat() {
        Player player3 = addOpponent("Charlie");
        Permanent master = addCreatureReady(player1, new TheMasterMultiplied());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(player2, master);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player3.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    @Test
    void myriadCreatesNoTokensInATwoPlayerGame() {
        Permanent master = addCreatureReady(player1, new TheMasterMultiplied());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(master);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void myriadTokenCreationCanBeDeclined() {
        addOpponent("Charlie");
        Permanent master = addCreatureReady(player1, new TheMasterMultiplied());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(player2, master);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(master);
    }

    @Test
    void opponentControlledMyriadTriggerStillExilesAStolenToken() {
        addOpponent("Charlie");
        Permanent master = addCreatureReady(player1, new TheMasterMultiplied());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(player2, master);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    @Test
    void ownSacrificeTriggerCannotOfferProtectedTokensAsChoices() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterMultiplied());
        Permanent token = addMasterToken(player1);
        harness.setHand(player1, List.of(new FleshbagMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent marauder = findPermanent(player1, "Fleshbag Marauder");
        PendingInteraction.MultiPermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(master.getId(), marauder.getId())
                .doesNotContain(token.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(marauder.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(master, token);
    }

    @Test
    void opponentSacrificeTriggerCanSacrificeCreatureTokens() {
        Permanent token = addMasterToken(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FleshbagMarauder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        harness.assertInGraveyard(player2, "Fleshbag Marauder");
    }

    private Permanent addMasterToken(Player controller) {
        TheMasterMultiplied card = new TheMasterMultiplied();
        card.setToken(true);
        return harness.addToBattlefieldAndReturn(controller, card);
    }

    private void declareAttackersAt(Player target, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(attackerIndex), java.util.Map.of(attackerIndex, target.getId()));
    }

    private Player addOpponent(String name) {
        Player opponent = new Player(UUID.randomUUID(), name);
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(name);
        gd.playerIdToName.put(opponent.getId(), name);
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerCommandZones.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }
}
