package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldenHind.class})
class GoldenHindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Golden Hind produces one green mana")
    void tappingProducesGreenMana() {
        Permanent perm = addCreatureReady(player1, new GoldenHind());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Golden Hind cannot tap for mana while summoning sick")
    void summoningSickCannotTap() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GoldenHind());

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped Golden Hind cannot produce additional mana")
    void cannotActivateTwiceWithoutUntapping() {
        Permanent perm = addCreatureReady(player1, new GoldenHind());

        gs.tapPermanent(gd, player1, 0);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Golden Hind can produce mana again after untapping")
    void canActivateAgainAfterUntapping() {
        Permanent perm = addCreatureReady(player1, new GoldenHind());
        perm.tap();

        harness.performUntapStep(player1);
        assertThat(perm.isTapped()).isFalse();
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
