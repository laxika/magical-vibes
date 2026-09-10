package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KazanduMammoth.class, KazanduValley.class, Forest.class})
class KazanduMammothTest extends BaseCardTest {

    @Test
    void mammothGetsPlusTwoPlusTwoWhenLandEnters() {
        Permanent mammoth = harness.addToBattlefieldAndReturn(player1, new KazanduMammoth());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(mammoth.getEffectivePower()).isEqualTo(5);
        assertThat(mammoth.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void valleyEntersTappedAndProducesGreenMana() {
        harness.setHand(player1, List.of(new KazanduMammoth()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent valley = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(valley.getCard()).isInstanceOf(KazanduValley.class);
        assertThat(valley.isTapped()).isTrue();

        valley.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.GREEN)).isEqualTo(1);
    }
}
