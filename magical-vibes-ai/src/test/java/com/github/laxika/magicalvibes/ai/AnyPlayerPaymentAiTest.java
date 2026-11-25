package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.d.DesecrationDemon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VexingDevil;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** How the AIs answer an "any player / any opponent may [pay something other than mana]" question. */
@DisplayName("AI answers to any-player non-mana payments")
class AnyPlayerPaymentAiTest extends HardAiDecisionEngineTestSupport {

    @Test
    @DisplayName("The base AI declines Desecration Demon's sacrifice once another opponent chose to sacrifice")
    void baseAiDeclinesASacrificeAnotherOpponentAlreadyChose() {
        Player player3 = addThirdPlayer();
        FakeConnection aiConnection = new FakeConnection("ai-easy-any-player-payment");
        harness.getSessionManager().registerPlayer(aiConnection, player3.getId(), player3.getUsername());
        EasyAiDecisionEngine ai = new EasyAiDecisionEngine(
                gd.id, player3, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(),
                harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(),
                harness.getCastingPermissionService(), harness.getTargetValidationService(),
                harness.getTargetLegalityService());
        harness.addToBattlefield(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player3, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player3.getId());
        gd.status = GameStatus.RUNNING;

        ai.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player3.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The hard AI at 4 life doesn't take Vexing Devil's 4 damage")
    void hardAiDoesNotPayDamageEqualToItsLife() {
        HardAiDecisionEngine ai = createHardAi(player2);
        gd.status = GameStatus.RUNNING;
        harness.setLife(player2, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new VexingDevil(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        ai.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Vexing Devil"));
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
