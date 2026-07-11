package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaoRenWeiCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB causes controller to lose 3 life")
    void etbLosesThreeLife() {
        harness.setHand(player1, List.of(new CaoRenWeiCommander()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cao Ren, Wei Commander"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }
}
