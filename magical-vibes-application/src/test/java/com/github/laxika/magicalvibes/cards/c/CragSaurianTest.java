package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CragSaurianTest extends BaseCardTest {

    @Test
    @DisplayName("spell damage makes its controller gain control after the trigger resolves")
    void spellDamageChangesControl() {
        harness.addToBattlefield(player2, new CragSaurian());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID cragSaurianId = harness.getPermanentId(player2, "Crag Saurian");
        harness.castInstant(player1, 0, cragSaurianId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crag Saurian");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Crag Saurian"));
    }
}
