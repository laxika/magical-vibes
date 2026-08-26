package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SolRing.class)
class SolRingTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Sol Ring produces two colorless mana")
    void tappingProducesTwoColorlessMana() {
        Permanent solRing = harness.addToBattlefieldAndReturn(player1, new SolRing());
        solRing.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(solRing.isTapped()).isTrue();
    }
}
