package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({IronwillForger.class, GrizzlyBears.class})
class IronwillForgerTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant targets a nonlegendary creature you control and grants it myriad")
    void lieutenantGrantsMyriadToLegalTarget() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addReadyCreature(player1, commander);
        addReadyCreature(player1, new IronwillForger());
        Permanent target = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

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
    }

    @Test
    @DisplayName("Lieutenant does nothing without a commander")
    void doesNotTriggerWithoutCommander() {
        addReadyCreature(player1, new IronwillForger());
        addReadyCreature(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lieutenant rejects an opponent's creature as a target")
    void rejectsOpponentCreatureTarget() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addReadyCreature(player1, commander);
        addReadyCreature(player1, new IronwillForger());
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
