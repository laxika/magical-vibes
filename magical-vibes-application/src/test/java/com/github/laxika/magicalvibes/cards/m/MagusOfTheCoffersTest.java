package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheCoffers.class, Swamp.class})
class MagusOfTheCoffersTest extends BaseCardTest {

    @Test
    @DisplayName("Adds black mana for each Swamp you control")
    void addsBlackManaForEachControlledSwamp() {
        harness.addToBattlefield(player1, new MagusOfTheCoffers());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        Permanent magus = findPermanent(player1, "Magus of the Coffers");
        magus.setSummoningSick(false);
        int magusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(magus);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, magusIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no black mana when you control no Swamps")
    void addsNoManaWithNoControlledSwamps() {
        harness.addToBattlefield(player1, new MagusOfTheCoffers());
        Permanent magus = gd.playerBattlefields.get(player1.getId()).getFirst();
        magus.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
