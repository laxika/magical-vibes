package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerumCoreChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Serum-Core Chimera")
    void noncreatureSpellPutsOilCounter() {
        Permanent chimera = addReadyChimera(0);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(chimera.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put an oil counter on Serum-Core Chimera")
    void creatureSpellDoesNotPutOilCounter() {
        Permanent chimera = addReadyChimera(0);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(chimera.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Removing three oil counters draws, discards a nonland, and deals 3 damage")
    void removesOilCountersDrawsDiscardsAndDealsDamage() {
        Permanent chimera = addReadyChimera(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerumCoreChimera());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(chimera.getCounterCount(CounterType.OIL)).isZero();
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the discard still draws a card but deals no damage")
    void mayDeclineDiscard() {
        Permanent chimera = addReadyChimera(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerumCoreChimera());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(chimera.getCounterCount(CounterType.OIL)).isZero();
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The reflexive damage trigger cannot target a player")
    void reflexiveDamageCannotTargetPlayer() {
        addReadyChimera(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChimera(int oilCounters) {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new SerumCoreChimera());
        chimera.setSummoningSick(false);
        chimera.setCounterCount(CounterType.OIL, oilCounters);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return chimera;
    }
}
