package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CrystalQuarry.class)
class CrystalQuarryTest extends BaseCardTest {

    @Test
    void tappingAddsColorlessMana() {
        var quarry = harness.addToBattlefieldAndReturn(player1, new CrystalQuarry());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(quarry.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingFiveGenericManaAddsOneOfEachColor() {
        var quarry = harness.addToBattlefieldAndReturn(player1, new CrystalQuarry());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(quarry.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingFiveGenericManaCanUseColoredMana() {
        harness.addToBattlefield(player1, new CrystalQuarry());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fiveManaAbilityRequiresFiveGenericMana() {
        harness.addToBattlefield(player1, new CrystalQuarry());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Crystal Quarry");
    }
}
