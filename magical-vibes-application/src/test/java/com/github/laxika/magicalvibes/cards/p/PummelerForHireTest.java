package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PummelerForHireTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains life equal to the greatest power among Giants you control")
    void gainsLifeForGreatestControlledGiantPower() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.setHand(player1, List.of(new PummelerForHire()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("ETB ignores non-Giants and Giants controlled by an opponent")
    void ignoresNonGiantsAndOpponents() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PummelerForHire()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }
}
