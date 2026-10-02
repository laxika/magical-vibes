package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaureanMauler.class, PricklyBoggart.class, Bitterblossom.class})
class TaureanMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent casting a spell triggers may ability and accepting adds a counter")
    void opponentSpellAcceptedAddsCounter() {
        harness.addToBattlefield(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent mauler = getMauler();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castFromHand(player2, new PricklyBoggart(), "{B}");

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

        Permanent mauler = getMauler();

        harness.castFromHand(player2, new PricklyBoggart(), "{B}");

        assertThat(gd.pendingMayAbilities).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell also triggers the may ability")
    void opponentNoncreatureSpellTriggers() {
        harness.addToBattlefield(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent mauler = getMauler();

        harness.castFromHand(player2, new Bitterblossom(), "{1}{B}");

        assertThat(gd.pendingMayAbilities).hasSize(1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller casting a spell does not trigger Taurean Mauler")
    void controllerSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new TaureanMauler());

        Permanent mauler = getMauler();

        harness.castFromHand(player1, new PricklyBoggart(), "{B}");

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent getMauler() {
        return findPermanent(player1, "Taurean Mauler");
    }
}
