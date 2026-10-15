package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FogBank;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfDoom.class, Forest.class, FogBank.class, DarksteelIngot.class})
class EyeOfDoomTest extends BaseCardTest {

    @Test
    void eachPlayerCanChooseAnyPlayersNonlandPermanentForDoomCounter() {
        Permanent player1Chosen = harness.addToBattlefieldAndReturn(player1, new FogBank());
        Permanent player1Other = harness.addToBattlefieldAndReturn(player1, new FogBank());
        harness.addToBattlefield(player1, new Forest());
        Permanent player2Chosen = harness.addToBattlefieldAndReturn(player2, new FogBank());
        Permanent player2Other = harness.addToBattlefieldAndReturn(player2, new FogBank());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new EyeOfDoom(), "{4}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent eye = findPermanent(player1, "Eye of Doom");
        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactlyInAnyOrder(
                eye.getId(), player1Chosen.getId(), player1Other.getId(), player2Chosen.getId(), player2Other.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player2Chosen.getId()));
        assertThat(player2Chosen.getCounterCount(CounterType.DOOM)).isZero();

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactlyInAnyOrder(eye.getId(), player1Chosen.getId(), player1Other.getId(), player2Chosen.getId(), player2Other.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player1Chosen.getId()));

        assertThat(player1Chosen.getCounterCount(CounterType.DOOM)).isEqualTo(1);
        assertThat(player2Chosen.getCounterCount(CounterType.DOOM)).isEqualTo(1);
        assertThat(player1Other.getCounterCount(CounterType.DOOM)).isZero();
        assertThat(player2Other.getCounterCount(CounterType.DOOM)).isZero();
    }

    @Test
    void sacrificeAbilityDestroysEveryPermanentWithDoomCounter() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfDoom());
        eye.setSummoningSick(false);
        Permanent markedOwn = harness.addToBattlefieldAndReturn(player1, new FogBank());
        Permanent markedOpponent = harness.addToBattlefieldAndReturn(player2, new FogBank());
        Permanent unmarked = harness.addToBattlefieldAndReturn(player2, new FogBank());
        markedOwn.setCounterCount(CounterType.DOOM, 1);
        markedOpponent.setCounterCount(CounterType.DOOM, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eye of Doom");
        harness.assertInGraveyard(player1, "Fog Bank");
        harness.assertInGraveyard(player2, "Fog Bank");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unmarked);
    }

    @Test
    void bothPlayersCanChooseTheSamePermanent() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new FogBank());
        harness.addToBattlefield(player2, new FogBank());
        harness.castFromHand(player1, new EyeOfDoom(), "{4}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        assertThat(chosen.getCounterCount(CounterType.DOOM)).isZero();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(chosen.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    @Test
    void loneEyeReceivesBothCountersWithoutAnInteractiveChoice() {
        harness.castFromHand(player1, new EyeOfDoom(), "{4}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Eye of Doom").getCounterCount(CounterType.DOOM)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void sacrificeIsPaidBeforeDestructionAndMarkedLandsAreDestroyed() {
        harness.addToBattlefield(player1, new EyeOfDoom());
        Permanent markedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        markedLand.setCounterCount(CounterType.DOOM, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Eye of Doom");
        harness.assertOnBattlefield(player2, "Forest");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void indestructiblePermanentSurvivesWithItsDoomCounter() {
        harness.addToBattlefield(player1, new EyeOfDoom());
        Permanent ingot = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        ingot.setCounterCount(CounterType.DOOM, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eye of Doom");
        harness.assertOnBattlefield(player2, "Darksteel Ingot");
        assertThat(ingot.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }
}
