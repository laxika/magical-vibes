package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalCoffers.class, Swamp.class})
class CabalCoffersTest extends BaseCardTest {

    @Test
    @DisplayName("Adds black mana for each Swamp you control")
    void addsBlackManaForEachControlledSwamp() {
        harness.addToBattlefield(player1, new CabalCoffers());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        Permanent coffers = findPermanent(player1, "Cabal Coffers");
        coffers.setSummoningSick(false);
        int coffersIndex = gd.playerBattlefields.get(player1.getId()).indexOf(coffers);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, coffersIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no black mana when you control no Swamps")
    void addsNoManaWithNoControlledSwamps() {
        harness.addToBattlefield(player1, new CabalCoffers());
        Permanent coffers = gd.playerBattlefields.get(player1.getId()).getFirst();
        coffers.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
