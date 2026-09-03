package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightspearZealot.class, Ornithopter.class})
class BrightspearZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 after its controller casts a second spell this turn")
    void getsBoostAfterSecondSpell() {
        harness.setHand(player1, List.of(new BrightspearZealot(), new Ornithopter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent zealot = findZealot();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the boost before its controller casts a second spell")
    void hasNoBoostAfterOneSpell() {
        harness.setHand(player1, List.of(new BrightspearZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent zealot = findZealot();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
    }

    private Permanent findZealot() {
        return findPermanent(player1, "Brightspear Zealot");
    }
}
