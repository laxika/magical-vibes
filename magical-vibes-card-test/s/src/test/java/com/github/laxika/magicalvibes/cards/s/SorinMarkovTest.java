package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PersonalSanctuary;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorinMarkov.class, RuneclawBear.class, Unsummon.class, PersonalSanctuary.class})
class SorinMarkovTest extends BaseCardTest {

    @Test
    @DisplayName("+2 deals 2 damage to target player and gains 2 life")
    void plusTwoDamagesPlayerAndGainsLife() {
        Permanent sorin = addReadySorin(player1);
        gd.playerLifeTotals.put(player1.getId(), 15);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("+2 can deal 2 damage to a creature and still gains 2 life")
    void plusTwoDamagesCreature() {
        addReadySorin(player1);
        gd.playerLifeTotals.put(player1.getId(), 15);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("-3 sets target opponent's life total to 10")
    void minusThreeSetsOpponentLifeToTen() {
        Permanent sorin = addReadySorin(player1);
        gd.playerLifeTotals.put(player2.getId(), 30);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("-3 raises a low opponent's life total up to 10")
    void minusThreeRaisesLowLifeTotal() {
        addReadySorin(player1);
        gd.playerLifeTotals.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("-3 cannot target its controller")
    void minusThreeRejectsController() {
        addReadySorin(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 gives control of target player's next turn to Sorin's controller")
    void minusSevenTakesControlOfNextTurn() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("-7 cannot be activated with only starting loyalty")
    void minusSevenRequiresSevenLoyalty() {
        addReadySorin(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("loyalty");
    }

    @Test
    @DisplayName("+2 can target its controller and gains life after dealing damage")
    void plusTwoCanTargetController() {
        addReadySorin(player1);
        harness.setLife(player1, 15);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(gd.lifeGainedThisTurn.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("+2 still gains 2 life when its damage is prevented")
    void plusTwoGainsLifeWhenDamagePrevented() {
        addReadySorin(player1);
        harness.addToBattlefield(player1, new PersonalSanctuary());
        harness.setLife(player1, 15);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("+2 damages a planeswalker by removing loyalty counters")
    void plusTwoDamagesPlaneswalker() {
        addReadySorin(player1);
        Permanent opponentSorin = harness.addToBattlefieldAndReturn(player2, new SorinMarkov());
        opponentSorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player1, 15);

        harness.activateAbility(player1, 0, 0, null, opponentSorin.getId());
        harness.passBothPriorities();

        assertThat(opponentSorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("+2 gains no life when its only target leaves the battlefield")
    void plusTwoDoesNotGainLifeWithIllegalTarget() {
        Permanent sorin = addReadySorin(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setLife(player1, 15);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Runeclaw Bear");
        harness.assertLife(player1, 15);
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 rejects creature targets without paying loyalty")
    void minusThreeRejectsCreature() {
        Permanent sorin = addReadySorin(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only one loyalty ability can be activated each turn")
    void cannotActivateSecondLoyaltyAbilityInSameTurn() {
        addReadySorin(player1);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Loyalty abilities cannot be activated outside a main phase")
    void cannotActivateDuringUpkeep() {
        addReadySorin(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 can target its controller")
    void minusSevenCanTargetController() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(player1.getId(), player1.getId());
        harness.assertInGraveyard(player1, "Sorin Markov");
    }

    @Test
    @DisplayName("-7 controls the next turn even after Sorin dies, then expires")
    void minusSevenControlsOnlyNextTurnAfterSourceDies() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sorin Markov");
        assertThat(gd.mindControlledPlayerId).isNull();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.pendingTurnControl).isEmpty();

        harness.setHand(player2, List.of(new RuneclawBear()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.clearPriorityPassed();
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.mindControlledPlayerId).isNull();
        assertThat(gd.mindControllerPlayerId).isNull();
    }

    private Permanent addReadySorin(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SorinMarkov());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
