package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PardicDragon.class, ThinkTwice.class})
class PardicDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Pardic Dragon with two time counters")
    void suspendExilesWithTwoTimeCounters() {
        PardicDragon dragon = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dragon);
        assertThat(gd.exiledCardTimeCounters).containsEntry(dragon.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the owner's upkeep removes a suspend counter")
    void onlyOwnersUpkeepRemovesCounter() {
        PardicDragon dragon = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(dragon.getId(), 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(dragon.getId(), 1);
    }

    @Test
    @DisplayName("Removing the last counter allows casting Pardic Dragon for free with haste")
    void suspendCastsForFreeWithHaste() {
        PardicDragon dragon = suspendCard();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(dragon.getId());
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(dragon);
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(dragon.getId()))
                .findFirst().orElseThrow();
        assertThat(permanent.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves the dragon exiled and unable to trigger")
    void decliningSuspendCastStopsOpponentSpellTrigger() {
        PardicDragon dragon = suspendCard();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        castOpponentSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dragon);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(dragon.getId());
        harness.assertNotOnBattlefield(player1, "Pardic Dragon");
    }

    @Test
    @DisplayName("Pardic Dragon on the battlefield does not trigger for an opponent's spell")
    void battlefieldDragonDoesNotTrigger() {
        harness.addToBattlefield(player1, new PardicDragon());
        castOpponentSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }

    @Test
    @DisplayName("The red ability gives Pardic Dragon +1/+0 until end of turn")
    void redAbilityBoostsUntilEndOfTurn() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new PardicDragon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The opponent who casts a spell may put a time counter on suspended Pardic Dragon")
    void opponentMayPutTimeCounterOnSuspendedDragon() {
        PardicDragon dragon = suspendCard();
        castOpponentSpell();

        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(dragon.getId(), 3);
    }

    @Test
    @DisplayName("Declining Pardic Dragon's time-counter ability leaves its counters unchanged")
    void decliningTimeCounterAbilityLeavesCountersUnchanged() {
        PardicDragon dragon = suspendCard();
        castOpponentSpell();

        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.exiledCardTimeCounters).containsEntry(dragon.getId(), 2);
    }

    @Test
    @DisplayName("Pardic Dragon's trigger does nothing if it stops being suspended before resolution")
    void timeCounterTriggerDoesNothingAfterSuspensionEnds() {
        PardicDragon dragon = suspendCard();
        castOpponentSpell();

        gd.exiledCardTimeCounters.remove(dragon.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(dragon.getId());

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Pardic Dragon does not trigger when its owner casts a spell")
    void ownerSpellDoesNotTrigger() {
        PardicDragon dragon = suspendCard();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCardTimeCounters).containsEntry(dragon.getId(), 2);
    }

    private PardicDragon suspendCard() {
        PardicDragon dragon = new PardicDragon();
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateHandAbility(player1, 0, null);
        return dragon;
    }

    private void castOpponentSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new ThinkTwice(), "{1}{U}");
    }
}
