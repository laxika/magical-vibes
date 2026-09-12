package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RofellosLlanowarEmissary.class, Forest.class, Plains.class})
class RofellosLlanowarEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one green mana for each Forest its controller controls")
    void tappingAddsManaForForestsControlled() {
        Permanent rofellos = addCreatureReady(player1, new RofellosLlanowarEmissary());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(rofellos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds no green mana when its controller controls no Forests")
    void tappingAddsNoManaWithoutControlledForests() {
        Permanent rofellos = addCreatureReady(player1, new RofellosLlanowarEmissary());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(rofellos.isTapped()).isTrue();
    }
}
