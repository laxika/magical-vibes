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
}
