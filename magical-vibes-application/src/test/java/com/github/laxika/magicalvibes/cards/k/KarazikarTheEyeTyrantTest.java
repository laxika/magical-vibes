package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarazikarTheEyeTyrant.class, GrizzlyBears.class})
class KarazikarTheEyeTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking a player taps and goads a creature that player controls")
    void attackingPlayerTapsAndGoadsTheirCreature() {
        addCreatureReady(player1, new KarazikarTheEyeTyrant());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The opponent attacking you does not trigger the second ability")
    void opponentAttackingControllerDoesNotTriggerSecondAbility() {
        addCreatureReady(player1, new KarazikarTheEyeTyrant());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void anotherCreatureAttackingTriggersWithoutKarazikarAttacking() {
        harness.addToBattlefield(player1, new KarazikarTheEyeTyrant());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    void multipleCreaturesAttackingOnePlayerGoadOnlyOneCreature() {
        harness.addToBattlefield(player1, new KarazikarTheEyeTyrant());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    void opponentAttackingAnotherOpponentRewardsBothPlayers() {
        UUID defender = addOpponent("Third opponent");
        prepareOpponentAttack();
        addCreatureReady(player2, new GrizzlyBears());

        declareOpponentAttack(List.of(0), Map.of(0, defender));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(defender)).isEmpty();
        assertThat(gd.playerLifeTotals.get(defender)).isEqualTo(20);
    }

    @Test
    void opponentAttackingTwoOtherOpponentsRewardsBothPlayersTwice() {
        UUID firstDefender = addOpponent("Third opponent");
        UUID secondDefender = addOpponent("Fourth opponent");
        prepareOpponentAttack();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareOpponentAttack(List.of(0, 1), Map.of(0, firstDefender, 1, secondDefender));
        harness.inMutationScope(() -> {
            while (!gd.stack.isEmpty()) {
                harness.getStackResolutionService().resolveTopOfStack(gd);
            }
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void removingAttackersBeforeResolutionDoesNotUndoTheAttackTrigger() {
        UUID defender = addOpponent("Third opponent");
        prepareOpponentAttack();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareOpponentAttack(List.of(0), Map.of(0, defender));
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        gd.playerGraveyards.get(player2.getId()).add(attacker.getCard());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    private void prepareOpponentAttack() {
        harness.addToBattlefield(player1, new KarazikarTheEyeTyrant());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
    }

    private void declareOpponentAttack(List<Integer> indices, Map<Integer, UUID> targets) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, indices, targets);
    }

    private UUID addOpponent(String name) {
        UUID id = UUID.randomUUID();
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add(name);
        gd.playerIdToName.put(id, name);
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerLifeTotals.put(id, 20);
        return id;
    }
}
