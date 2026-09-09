package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KozileksChanneler.class)
class KozileksChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Kozilek's Channeler adds two colorless mana")
    void tappingAddsTwoColorlessMana() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new KozileksChanneler());
        channeler.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(channeler.isTapped()).isTrue();
    }
}
