package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireSprites.class})
class FireSpritesTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {G} and tapping Fire Sprites adds {R}")
    void addsRedMana() {
        Permanent sprites = harness.addToBattlefieldAndReturn(player1, new FireSprites());
        sprites.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(sprites.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fire Sprites cannot activate without green mana")
    void requiresGreenMana() {
        Permanent sprites = harness.addToBattlefieldAndReturn(player1, new FireSprites());
        sprites.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(sprites.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Fire Sprites cannot activate while summoning sick")
    void summoningSickCannotActivate() {
        Permanent sprites = harness.addToBattlefieldAndReturn(player1, new FireSprites());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(sprites.isTapped()).isFalse();
    }
}
