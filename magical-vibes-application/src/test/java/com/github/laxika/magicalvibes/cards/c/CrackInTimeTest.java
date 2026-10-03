package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrackInTime.class, GrizzlyBears.class})
class CrackInTimeTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield trigger exiles an opposing creature until Crack in Time leaves")
    void exilesCreatureOnEntryAndReturnsItWhenSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent crack = castAndResolve(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crack));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The first-main-phase trigger exiles a new opposing creature")
    void exilesCreatureAtBeginningOfFirstMainPhase() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(firstTarget.getId());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToPrecombatMain(player1);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondTarget.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(secondTarget.getCard().getId()));
    }

    @Test
    @DisplayName("The first-main-phase trigger does not happen during an opponent's first main phase")
    void doesNotTriggerDuringOpponentsFirstMainPhase() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(firstTarget.getId());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToPrecombatMain(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondTarget.getId()));
    }

    @Test
    @DisplayName("Crack in Time cannot target a creature controlled by its caster")
    void targetMustBeCreatureAnOpponentControls() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new CrackInTime()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersWithThreeTimeCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent crack = castAndResolve(target.getId());

        assertThat(crack.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void controllerUpkeepRemovesATimeCounter() {
        Permanent crack = harness.addToBattlefieldAndReturn(player1, new CrackInTime());
        crack.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(crack.getCounterCount(CounterType.TIME)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Crack in Time");
    }

    @Test
    void opponentsUpkeepDoesNotRemoveATimeCounter() {
        Permanent crack = harness.addToBattlefieldAndReturn(player1, new CrackInTime());
        crack.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(crack.getCounterCount(CounterType.TIME)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Crack in Time");
    }

    @Test
    void lastTimeCounterSacrificesSourceAndReturnsExiledCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent crack = castAndResolve(target.getId());
        crack.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Crack in Time");
        harness.assertInGraveyard(player1, "Crack in Time");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void returnsEveryCreatureExiledByTheSameSource() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent crack = castAndResolve(firstTarget.getId());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        advanceToPrecombatMain(player1);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        resolveAllTriggers();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crack));

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void sourceLeavingBeforeMainPhaseTriggerResolvesDoesNotExileTarget() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent crack = castAndResolve(firstTarget.getId());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        advanceToPrecombatMain(player1);
        harness.handlePermanentChosen(player1, secondTarget.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crack));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondTarget);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void canEnterWithoutAnOpposingCreature() {
        harness.setHand(player1, List.of(new CrackInTime()));
        addMana();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Crack in Time");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void sourceLeavingBeforeEntryTriggerResolvesDoesNotExileTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrackInTime()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent crack = findPermanent(player1, "Crack in Time");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crack));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void upkeepWithoutTimeCountersDoesNotSacrificeSource() {
        harness.addToBattlefield(player1, new CrackInTime());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Crack in Time");
    }
    private Permanent castAndResolve(UUID targetId) {
        harness.setHand(player1, List.of(new CrackInTime()));
        addMana();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Crack in Time");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
