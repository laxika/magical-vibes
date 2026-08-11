package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NantukoElderTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one colorless and one green mana")
    void manaAbilityAddsColorlessAndGreen() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new NantukoElder());
        elder.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(elder.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
