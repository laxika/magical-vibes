package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SuChiCaveGuardTest extends BaseCardTest {

    @Test
    @DisplayName("When Su-Chi Cave Guard dies, it adds eight colorless mana that survives a phase transition")
    void diesAddsEightPersistentColorlessMana() {
        harness.addToBattlefield(player1, new SuChiCaveGuard());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Su-Chi Cave Guard");
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(8);
        assertThat(pool.getPersistentMana(ManaColor.COLORLESS)).isEqualTo(8);

        pool.add(ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(8);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }
}
