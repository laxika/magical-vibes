package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProperBurial.class, Ornithopter.class, Shock.class})
class ProperBurialTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to the toughness of a dying creature they control")
    void gainsLifeEqualToDyingCreatureToughness() {
        harness.addToBattlefield(player1, new ProperBurial());
        harness.addToBattlefield(player1, new Ornithopter());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Ornithopter"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void doesNotTriggerOnOpponentCreatureDeath() {
        harness.addToBattlefield(player1, new ProperBurial());
        harness.addToBattlefield(player2, new Ornithopter());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Ornithopter"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }
}
