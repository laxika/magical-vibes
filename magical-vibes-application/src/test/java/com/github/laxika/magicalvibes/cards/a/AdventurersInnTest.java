package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AdventurersInn.class)
class AdventurersInnTest extends BaseCardTest {

    @Test
    void entersAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AdventurersInn()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    void tapsForColorlessMana() {
        Permanent inn = new Permanent(new AdventurersInn());
        inn.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(inn);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(inn.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
