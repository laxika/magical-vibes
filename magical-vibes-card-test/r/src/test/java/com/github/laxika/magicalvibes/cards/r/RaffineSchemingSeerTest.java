package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BackupAgent;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaffineSchemingSeer.class, BackupAgent.class, Mountain.class, Murder.class})
class RaffineSchemingSeerTest extends BaseCardTest {

    @Test
    void attacksMakeTargetAttackerConniveForEachAttacker() {
        Permanent raffine = addCreatureReady(player1, new RaffineSchemingSeer());
        Permanent attacker = addCreatureReady(player1, new BackupAgent());
        Permanent otherAttacker = addCreatureReady(player1, new BackupAgent());

        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain(), new BackupAgent()));

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(attacker.getId(), otherAttacker.getId())
                .doesNotContain(raffine.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        discardByName("Mountain");
        discardByName("Backup Agent");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void connivePutsCountersOnlyAfterAllCardsAreDiscarded() {
        addCreatureReady(player1, new RaffineSchemingSeer());
        Permanent attacker = addCreatureReady(player1, new BackupAgent());
        addCreatureReady(player1, new BackupAgent());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BackupAgent(), new BackupAgent()));

        declareAttackers(List.of(1, 2));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        discardByName("Backup Agent");
        int countersBeforeLastDiscard = attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        discardByName("Backup Agent");

        assertThat(countersBeforeLastDiscard).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void raffineCanConniveItselfAndDiscardingOnlyLandsAddsNoCounters() {
        Permanent raffine = addCreatureReady(player1, new RaffineSchemingSeer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, raffine.getId());
        harness.passBothPriorities();
        discardByName("Mountain");

        assertThat(raffine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    void conniveCountsAttackersAtResolutionAfterAnotherAttackerDies() {
        addCreatureReady(player1, new RaffineSchemingSeer());
        Permanent attacker = addCreatureReady(player1, new BackupAgent());
        Permanent otherAttacker = addCreatureReady(player1, new BackupAgent());
        harness.setHand(player1, List.of(new Murder()));
        harness.setLibrary(player1, List.of(new BackupAgent(), new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(1, 2));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.castAndResolveInstant(player1, 0, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        discardByName("Backup Agent");
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void removingTheTargetPreventsDrawingAndDiscarding() {
        addCreatureReady(player1, new RaffineSchemingSeer());
        Permanent attacker = addCreatureReady(player1, new BackupAgent());
        harness.setHand(player1, List.of(new Murder()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void wardCountersAnOpponentsSpellWithoutSpareMana() {
        Permanent raffine = harness.addToBattlefieldAndReturn(player1, new RaffineSchemingSeer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, raffine.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Raffine, Scheming Seer");
        harness.assertInGraveyard(player2, "Murder");
    }

    @Test
    void payingOneManaForWardAllowsTheOpponentsSpellToResolve() {
        Permanent raffine = harness.addToBattlefieldAndReturn(player1, new RaffineSchemingSeer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, raffine.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Raffine, Scheming Seer");
        harness.assertInGraveyard(player2, "Murder");
    }

    @Test
    void flyingPreventsAGroundCreatureFromBlockingRaffine() {
        Permanent raffine = addCreatureReady(player1, new RaffineSchemingSeer());
        addCreatureReady(player2, new BackupAgent());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, raffine.getId());
            harness.passBothPriorities();
            discardByName("Mountain");
        });
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void opponentsAttacksDoNotTriggerRaffine() {
        addCreatureReady(player1, new RaffineSchemingSeer());
        addCreatureReady(player2, new BackupAgent());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void removingRaffineDoesNotStopItsAlreadyTriggeredAbility() {
        Permanent raffine = addCreatureReady(player1, new RaffineSchemingSeer());
        Permanent attacker = addCreatureReady(player1, new BackupAgent());
        harness.setHand(player1, List.of(new Murder()));
        harness.setLibrary(player1, List.of(new BackupAgent()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.castAndResolveInstant(player1, 0, raffine.getId());
        harness.passBothPriorities();
        discardByName("Backup Agent");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Raffine, Scheming Seer");
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
