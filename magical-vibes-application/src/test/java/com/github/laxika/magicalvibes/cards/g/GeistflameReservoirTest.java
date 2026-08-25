package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GeistflameReservoir.class, Island.class, Shock.class})
class GeistflameReservoirTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a charge counter on Geistflame Reservoir")
    void castingInstantAddsChargeCounter() {
        Permanent reservoir = addReadyReservoir(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing charge counters deals that much damage to any target")
    void removesCountersToDealDamage() {
        Permanent reservoir = addReadyReservoir(player1);
        reservoir.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(reservoir.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The library ability exiles the top card and grants play permission this turn")
    void exilesTopCardAndGrantsPlayPermission() {
        Permanent reservoir = addReadyReservoir(player1);
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(reservoir.isTapped()).isTrue();
    }

    private Permanent addReadyReservoir(Player player) {
        Permanent reservoir = new Permanent(new GeistflameReservoir());
        reservoir.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(reservoir);
        return reservoir;
    }
}
