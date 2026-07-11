package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaureanMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent casting a spell triggers may ability and accepting adds a counter")
    void opponentSpellAcceptedAddsCounter() {
        harness.addToBattlefield(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        Permanent mauler = getMauler();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player2, 0);

        assertThat(gd.pendingMayAbilities).hasSize(1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, mauler)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, mauler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the may ability does not add a counter")
    void opponentSpellDeclinedDoesNotAddCounter() {
        harness.addToBattlefield(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        Permanent mauler = getMauler();

        harness.castCreature(player2, 0);

        assertThat(gd.pendingMayAbilities).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Controller casting a spell does not trigger Taurean Mauler")
    void controllerSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new TaureanMauler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent mauler = getMauler();

        harness.castCreature(player1, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent getMauler() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Taurean Mauler"))
                .findFirst()
                .orElseThrow();
    }
}
