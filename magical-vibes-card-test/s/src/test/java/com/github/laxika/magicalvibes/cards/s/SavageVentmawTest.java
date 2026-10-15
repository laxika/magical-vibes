package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SavageVentmaw.class)
class SavageVentmawTest extends BaseCardTest {

    @Test
    void attackingAddsPersistentRedAndGreenMana() {
        addCreatureReady(player1, new SavageVentmaw());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(3);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(pool.getPersistentMana(ManaColor.RED)).isEqualTo(3);
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isEqualTo(3);

        pool.add(ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.RED)).isEqualTo(3);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void manaExpiresAtTheEndOfTheTurn() {
        addCreatureReady(player1, new SavageVentmaw());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.END_STEP);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(3);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    void attackingOnTheOtherPlayersTurnAwardsManaToThatController() {
        addCreatureReady(player2, new SavageVentmaw());
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        ManaPool attackerPool = gd.playerManaPools.get(player2.getId());
        ManaPool defenderPool = gd.playerManaPools.get(player1.getId());
        assertThat(attackerPool.get(ManaColor.RED)).isEqualTo(3);
        assertThat(attackerPool.get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(defenderPool.get(ManaColor.RED)).isZero();
        assertThat(defenderPool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    void attackTriggerResolvesAfterTheSourceLeavesTheBattlefield() {
        var ventmaw = addCreatureReady(player1, new SavageVentmaw());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(ventmaw);
        gd.playerGraveyards.get(player1.getId()).add(ventmaw.getCard());
        resolveAllTriggers();

        assertThat(pool.get(ManaColor.RED)).isEqualTo(3);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
    }
}
