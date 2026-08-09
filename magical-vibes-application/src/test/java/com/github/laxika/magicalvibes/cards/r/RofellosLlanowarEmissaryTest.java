package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RofellosLlanowarEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one green mana for each Forest its controller controls")
    void tappingAddsManaForForestsControlled() {
        Permanent rofellos = harness.addToBattlefieldAndReturn(player1, new RofellosLlanowarEmissary());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());
        rofellos.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(rofellos.isTapped()).isTrue();
    }
}
