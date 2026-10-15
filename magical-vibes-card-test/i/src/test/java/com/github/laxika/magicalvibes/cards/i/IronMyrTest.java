package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronMyr.class})
class IronMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Iron Myr produces one red mana")
    void tappingProducesRedMana() {
        addCreatureReady(player1, new IronMyr());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Iron Myr cannot tap for mana")
    void summoningSickCannotTap() {
        harness.addToBattlefield(player1, new IronMyr());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Iron Myr's mana ability resolves immediately without using the stack")
    void manaAbilityResolvesImmediately() {
        addCreatureReady(player1, new IronMyr());

        harness.tapPermanent(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("An already tapped Iron Myr cannot produce more mana")
    void alreadyTappedCannotTapAgain() {
        addCreatureReady(player1, new IronMyr());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Iron Myr can produce mana again after its controller's untap step")
    void canTapAgainAfterUntapping() {
        harness.addToBattlefield(player1, new IronMyr());
        harness.performUntapStep(player1);
        harness.tapPermanent(player1, 0);

        harness.performUntapStep(player1);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
