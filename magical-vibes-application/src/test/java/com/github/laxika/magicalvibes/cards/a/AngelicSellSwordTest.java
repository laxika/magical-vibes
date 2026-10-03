package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HumbleDefector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicSellSword.class, HumbleDefector.class})
class AngelicSellSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mercenary token")
    void enteringCreatesMercenaryToken() {
        harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    @DisplayName("Another nontoken creature entering creates a Mercenary token")
    void anotherNontokenCreatureEnteringCreatesMercenaryToken() {
        harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new HumbleDefector());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(2);
    }

    @Test
    @DisplayName("Attacking draws a card when power is at least six")
    void attackingWithSixPowerDraws() {
        Permanent angel = addCreatureReady(player1, new AngelicSellSword());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AngelicSellSword()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking does not draw a card when power is below six")
    void attackingBelowSixPowerDoesNotDraw() {
        addCreatureReady(player1, new AngelicSellSword());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AngelicSellSword()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void tokenCopyEnteringCreatesItsOwnMercenary() {
        AngelicSellSword tokenCopy = new AngelicSellSword();
        tokenCopy.setToken(true);
        harness.enterBattlefieldAndReturn(player1, tokenCopy);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    void anotherTokenCreatureEnteringDoesNotCreateMercenary() {
        harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        HumbleDefector token = new HumbleDefector();
        token.setToken(true);
        harness.enterBattlefieldAndReturn(player1, token);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    void opposingNontokenCreatureEnteringDoesNotCreateMercenary() {
        harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player2, new HumbleDefector());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
        assertThat(findPermanents(player2, "Mercenary")).isEmpty();
    }

    @Test
    void powerFallingBelowSixBeforeResolutionPreventsDraw() {
        Permanent angel = addCreatureReady(player1, new AngelicSellSword());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AngelicSellSword()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void gainingSixPowerAfterAttackingDoesNotCreateDrawTrigger() {
        Permanent angel = addCreatureReady(player1, new AngelicSellSword());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AngelicSellSword()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void mercenaryBoostsControlledCreatureUntilEndOfTurn() {
        Permanent angel = harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int initialPower = gqs.getEffectivePower(gd, angel);
        int initialToughness = gqs.getEffectiveToughness(gd, angel);

        harness.activateAbility(player1, 1, null, angel.getId());
        resolveAllTriggers();

        assertThat(mercenary.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(initialToughness);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(initialPower);
    }

    @Test
    void mercenaryCannotTargetOpposingCreature() {
        harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        findPermanent(player1, "Mercenary").setSummoningSick(false);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mercenaryCannotActivateDuringCombat() {
        Permanent angel = harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        findPermanent(player1, "Mercenary").setSummoningSick(false);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newlyCreatedMercenaryCannotActivateTapAbility() {
        Permanent angel = harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mercenaryCannotActivateDuringOpponentsMainPhase() {
        Permanent angel = harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        findPermanent(player1, "Mercenary").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mercenaryCannotActivateWithAnAbilityOnStack() {
        Permanent angel = harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new HumbleDefector());
        resolveAllTriggers();
        findPermanents(player1, "Mercenary").forEach(token -> token.setSummoningSick(false));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, angel.getId());
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 3, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();
    }
}
