package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({IronwillForger.class, GrizzlyBears.class, ZetalpaPrimalDawn.class})
class IronwillForgerTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant targets a nonlegendary creature you control and grants it myriad")
    void lieutenantGrantsMyriadToLegalTarget() {
        Card commander = new ZetalpaPrimalDawn();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        addCreatureReady(player1, new IronwillForger());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        UUID thirdPlayerId = addThirdPlayer();
        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.isTapped()
                        && permanent.isAttacking()
                        && permanent.getAttackTarget().equals(thirdPlayerId));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Lieutenant does nothing without a commander")
    void doesNotTriggerWithoutCommander() {
        addCreatureReady(player1, new IronwillForger());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lieutenant rejects an opponent's creature as a target")
    void rejectsOpponentCreatureTarget() {
        Card commander = new ZetalpaPrimalDawn();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        addCreatureReady(player1, new IronwillForger());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void excludesLegendaryCreaturesAndAllowsForgerItself() {
        Card commander = new ZetalpaPrimalDawn();
        gd.makeCommander(player1.getId(), commander);
        Permanent legendary = addCreatureReady(player1, commander);
        Permanent forger = addCreatureReady(player1, new IronwillForger());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(forger.getId()).doesNotContain(legendary.getId());
        harness.handlePermanentChosen(player1, forger.getId());
        harness.passBothPriorities();
    }

    @Test
    void commanderLeavingBeforeResolutionPreventsMyriad() {
        Card commander = new ZetalpaPrimalDawn();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        addCreatureReady(player1, new IronwillForger());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addThirdPlayer();

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(findPermanents(player1, "Grizzly Bears")).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void myriadTokenCreationCanBeDeclined() {
        Card commander = new ZetalpaPrimalDawn();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        addCreatureReady(player1, new IronwillForger());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addThirdPlayer();

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });
        assertThat(findPermanents(player1, "Grizzly Bears")).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        Card commander = new ZetalpaPrimalDawn();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        addCreatureReady(player1, new IronwillForger());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private UUID addThirdPlayer() {
        UUID id = UUID.randomUUID();
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
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return id;
    }
}
