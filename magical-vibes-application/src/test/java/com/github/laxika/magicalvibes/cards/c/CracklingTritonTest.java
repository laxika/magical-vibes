package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElspethSunsChampion;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CracklingTriton.class, TravelingPhilosopher.class, Island.class, ElspethSunsChampion.class})
class CracklingTritonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to a player")
    void sacrificesItselfAndDamagesPlayer() {
        addTriton(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Crackling Triton");
        harness.assertInGraveyard(player1, "Crackling Triton");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to a creature")
    void damagesCreature() {
        addTriton(player1);
        Permanent target = addCreatureReady(player2, new TravelingPhilosopher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        harness.assertInGraveyard(player2, "Traveling Philosopher");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addTriton(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent triton = harness.addToBattlefieldAndReturn(player1, new CracklingTriton());
        triton.setSummoningSick(true);
        triton.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.assertInGraveyard(player1, "Crackling Triton");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Deals 2 damage to a planeswalker")
    void damagesPlaneswalker() {
        addTriton(player1);
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethSunsChampion());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, elspeth.getId());
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Elspeth, Sun's Champion");
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void cannotActivateWithoutRedMana() {
        addTriton(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Crackling Triton");
        harness.assertNotInGraveyard(player1, "Crackling Triton");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with only two mana")
    void cannotActivateWithInsufficientMana() {
        addTriton(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Crackling Triton");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself but deals no damage after sacrificing itself")
    void selfTargetBecomesIllegalAfterSacrifice() {
        Permanent triton = addTriton(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, triton.getId());
        harness.assertInGraveyard(player1, "Crackling Triton");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addTriton(Player player) {
        return addCreatureReady(player, new CracklingTriton());
    }
}
