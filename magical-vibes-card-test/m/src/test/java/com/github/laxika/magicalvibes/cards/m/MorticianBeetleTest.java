package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.d.DreadStatuary;
import com.github.laxika.magicalvibes.cards.b.BloodthroneVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MorticianBeetle.class, DiabolicEdict.class, GrizzlyBears.class, BloodthroneVampire.class,
        DreadStatuary.class})
class MorticianBeetleTest extends BaseCardTest {

    @Test
    @DisplayName("When another player sacrifices a creature, Mortician Beetle may get a counter")
    void opponentSacrificeAddsCounter() {
        addCreatureReady(player1, new MorticianBeetle());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mortician Beetle's controller's creature sacrifice also triggers it")
    void controllerSacrificeAddsCounter() {
        addCreatureReady(player1, new MorticianBeetle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEdictAt(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining Mortician Beetle's trigger leaves it without a counter")
    void declineLeavesBeetleUnchanged() {
        addCreatureReady(player1, new MorticianBeetle());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An edict with no creature to sacrifice does not trigger Mortician Beetle")
    void noSacrificeDoesNotTrigger() {
        harness.addToBattlefield(player1, new MorticianBeetle());

        castEdictAt(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing a creature as an activation cost triggers Mortician Beetle once")
    void sacrificeAsCostTriggersOnce() {
        harness.addToBattlefield(player1, new MorticianBeetle());
        harness.addToBattlefield(player2, new BloodthroneVampire());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new BloodthroneVampire());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, sacrificed.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing an animated land to an edict triggers Mortician Beetle")
    void animatedLandSacrificeTriggers() {
        harness.addToBattlefield(player1, new MorticianBeetle());
        harness.addToBattlefield(player2, new DreadStatuary());
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        castEdictAt(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(morticianBeetle().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent morticianBeetle() {
        return findPermanent(player1, "Mortician Beetle");
    }

    private void castEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
