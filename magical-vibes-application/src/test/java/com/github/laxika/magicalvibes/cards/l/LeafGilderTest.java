package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeafGilderTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {G} produces one green mana")
    void tapAddsGreen() {
        harness.addToBattlefield(player1, new LeafGilder());

        Permanent gilder = gd.playerBattlefields.get(player1.getId()).getFirst();
        gilder.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gilder.isTapped()).isTrue();
    }
}
