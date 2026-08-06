package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkyshroudElfTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {G} — adds green mana and taps")
    void tapForGreen() {
        Permanent elf = addCreatureReady(player1, new SkyshroudElf());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(elf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{1}: Add {R} — filters generic mana into red without tapping")
    void filterForRed() {
        Permanent elf = addCreatureReady(player1, new SkyshroudElf());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(elf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("{1}: Add {W} — filters generic mana into white")
    void filterForWhite() {
        addCreatureReady(player1, new SkyshroudElf());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("{1} ability cannot be activated without mana to pay")
    void filterRequiresMana() {
        addCreatureReady(player1, new SkyshroudElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The {1} ability works while the elf is tapped")
    void filterWorksWhileTapped() {
        Permanent elf = addCreatureReady(player1, new SkyshroudElf());
        harness.addMana(player1, ManaColor.BLUE, 1);
        elf.tap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
