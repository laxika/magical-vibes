package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuddleTheEverChanging.class, GrizzlyBears.class, Opt.class})
class MuddleTheEverChangingTest extends BaseCardTest {

    @Test
    void copiesOnlyANonlegendaryCreatureYouControl() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(
                muddle.getId(), opposingCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();

        assertThat(muddle.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, muddle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, muddle)).isEqualTo(2);
    }

    @Test
    void myriadCreatesTappedAttackingTokenForTheOtherOpponentAndExilesItAtEndOfCombat() {
        UUID thirdPlayerId = addThirdPlayer(gd);
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castMuddleTriggerAndChoose(muddle, target);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(thirdPlayerId);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(),
                        DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    private void castMuddleTriggerAndChoose(Permanent muddle, Permanent target) {
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(muddle.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    private UUID addThirdPlayer(GameData gameData) {
        UUID playerId = UUID.randomUUID();
        gameData.playerIds.add(playerId);
        gameData.orderedPlayerIds.add(playerId);
        gameData.playerNames.add("Charlie");
        gameData.playerIdToName.put(playerId, "Charlie");
        gameData.playerBattlefields.put(playerId, gameData.newBattlefieldList());
        gameData.playerDecks.put(playerId, new ArrayList<>());
        gameData.playerHands.put(playerId, new ArrayList<>());
        gameData.playerGraveyards.put(playerId, new ArrayList<>());
        gameData.playerCommandZones.put(playerId, new ArrayList<>());
        gameData.playerCommanders.put(playerId, new ArrayList<>());
        gameData.playerManaPools.put(playerId, new ManaPool());
        gameData.playerLifeTotals.put(playerId, gameData.startingLife());
        gameData.playerAutoStopSteps.put(playerId, new HashSet<>());
        return playerId;
    }
}
