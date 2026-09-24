package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolverineBestThereIs.class, GrizzlyBears.class, Shock.class})
class WolverineBestThereIsTest extends BaseCardTest {

    @Test
    @DisplayName("Wolverine deals double combat damage to a player")
    void doublesCombatDamageToPlayer() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(wolverine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage to another creature gives Wolverine a +1/+1 counter at end step")
    void damageToCreatureGivesCounterAtEndStep() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());
        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(0);
        blockerCard.setToughness(2);
        addCreatureReady(player2, blockerCard);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(wolverine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(wolverine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage to a player does not satisfy Wolverine's creature-damage condition")
    void damageToPlayerDoesNotGiveCounter() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());
        declareAttackers(player1, List.of(0));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(wolverine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Regeneration saves Wolverine from lethal damage")
    void regenerationSavesWolverine() {
        Permanent wolverine = addCreatureReady(player1, new WolverineBestThereIs());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, wolverine.getId());

        harness.assertOnBattlefield(player1, "Wolverine, Best There Is");
        Permanent surviving = findPermanent(player1, "Wolverine, Best There Is");
        assertThat(surviving.getRegenerationShield()).isZero();
        assertThat(surviving.isTapped()).isTrue();
        assertThat(surviving.getMarkedDamage()).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
