package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VecTownshipsTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds colorless without locking the untap step")
    void colorlessAbilityDoesNotSkipUntap() {
        harness.addToBattlefield(player1, new VecTownships());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Green ability adds {G} and locks the next untap step")
    void greenAbilityAddsGreenAndSkipsUntap() {
        harness.addToBattlefield(player1, new VecTownships());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("White ability adds {W} and locks the next untap step")
    void whiteAbilityAddsWhiteAndSkipsUntap() {
        harness.addToBattlefield(player1, new VecTownships());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.getSkipUntapCount()).isGreaterThan(0);
    }
}
