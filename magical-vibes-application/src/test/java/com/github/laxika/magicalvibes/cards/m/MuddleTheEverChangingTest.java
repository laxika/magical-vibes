package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuddleTheEverChanging.class, GrizzlyBears.class, Opt.class, FaithlessLooting.class})
class MuddleTheEverChangingTest extends BaseCardTest {

    @Test
    void copiesOnlyANonlegendaryCreatureYouControl() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Opt(), "{U}");
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

        declareAttackers(player1, List.of(0));
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

    @Test
    void canChooseNoTargetEvenWhenALegalCreatureExists() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(muddle.getCard().getName()).isEqualTo("Muddle, the Ever-Changing");
    }

    @Test
    void targetRemovedBeforeResolutionDoesNotCauseACopy() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(muddle.getCard().getName()).isEqualTo("Muddle, the Ever-Changing");
    }

    @Test
    void targetThatIsNoLongerControlledByYouDoesNotCauseACopy() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(muddle.getCard().getName()).isEqualTo("Muddle, the Ever-Changing");
    }

    @Test
    void sorceryTriggersCopyBeforeTheSpellResolves() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new FaithlessLooting(), "{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(muddle.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Faithless Looting");
    }

    @Test
    void copyExpiresAndOriginalSpellCastAbilityReturnsNextTurn() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castMuddleTriggerAndChoose(muddle, target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(muddle.getCard().getName()).isEqualTo("Muddle, the Ever-Changing");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castMuddleTriggerAndChoose(muddle, target);
        assertThat(muddle.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void copiedMuddleDoesNotTriggerItsOriginalAbilityOnAnotherSpell() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castMuddleTriggerAndChoose(muddle, target);

        harness.castFromHand(player1, new Opt(), "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(muddle.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void myriadDoesNotCreateTokensWithOnlyOneOpponent() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castMuddleTriggerAndChoose(muddle, target);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void opponentsInstantDoesNotTriggerMuddle() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player2, new Opt(), "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(muddle.getCard().getName()).isEqualTo("Muddle, the Ever-Changing");
    }

    @Test
    void creatureSpellDoesNotTriggerMuddle() {
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(muddle.getCard().getName()).isEqualTo("Muddle, the Ever-Changing");
    }

    @Test
    void myriadTokenCreationCanBeDeclined() {
        addThirdPlayer(gd);
        Permanent muddle = addCreatureReady(player1, new MuddleTheEverChanging());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castMuddleTriggerAndChoose(muddle, target);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void castMuddleTriggerAndChoose(Permanent muddle, Permanent target) {
        harness.castFromHand(player1, new Opt(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(muddle.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
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
