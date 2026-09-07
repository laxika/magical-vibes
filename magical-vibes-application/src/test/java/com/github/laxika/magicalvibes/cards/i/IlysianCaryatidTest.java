package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IlysianCaryatid.class, AvatarOfMight.class})
class IlysianCaryatidTest extends BaseCardTest {

    @Test
    void addsOneManaWhenNoControlledCreatureHasPowerFour() {
        addCreatureReady(player1, new IlysianCaryatid());
        harness.addToBattlefield(player2, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void addsTwoManaOfOneColorWhenControllingCreatureWithPowerFour() {
        addCreatureReady(player1, new IlysianCaryatid());
        harness.addToBattlefield(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
