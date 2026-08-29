package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KrarkClanIronworksTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Krark-Clan Ironworks adds two colorless mana")
    void sacrificingSourceAddsTwoColorlessMana() {
        harness.addToBattlefield(player1, new KrarkClanIronworks());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Krark-Clan Ironworks");
    }

}
