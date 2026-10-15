package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LouvaqTheAberrant.class, GrizzlyBears.class, GiantGrowth.class})
class LouvaqTheAberrantTest extends BaseCardTest {

    @Test
    @DisplayName("Louvaq protects from modified creature permanents")
    void protectsFromModifiedCreatures() {
        Permanent louvaq = harness.addToBattlefieldAndReturn(player1, new LouvaqTheAberrant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSource(gd, louvaq, bears)).isFalse();

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasProtectionFromSource(gd, louvaq, bears)).isTrue();
    }

    @Test
    @DisplayName("At each player's end step, Louvaq may grow that player's creature")
    void putsCounterOnActivePlayersCreatureAtEndStep() {
        harness.addToBattlefield(player1, new LouvaqTheAberrant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Louvaq protects from a creature changed by Giant Growth")
    void protectsFromTemporaryPowerAndToughnessChange() {
        Permanent louvaq = harness.addToBattlefieldAndReturn(player1, new LouvaqTheAberrant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gqs.hasProtectionFromSource(gd, louvaq, bears)).isTrue();
    }

    @Test
    @DisplayName("A counter that changes no power, toughness, or ability does not grant protection")
    void doesNotProtectFromUnchangedCreatureWithChargeCounter() {
        Permanent louvaq = harness.addToBattlefieldAndReturn(player1, new LouvaqTheAberrant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasProtectionFromSource(gd, louvaq, bears)).isFalse();
    }

    @Test
    @DisplayName("Louvaq's controller may grow an opponent's creature on that opponent's end step")
    void putsCounterOnOpponentsCreatureAtTheirEndStep() {
        harness.addToBattlefield(player1, new LouvaqTheAberrant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player1, true);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Louvaq's controller may decline the counter")
    void mayDeclineCounter() {
        harness.addToBattlefield(player1, new LouvaqTheAberrant());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
