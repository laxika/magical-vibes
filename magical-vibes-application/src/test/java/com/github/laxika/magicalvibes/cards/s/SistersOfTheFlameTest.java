package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SistersOfTheFlame.class})
class SistersOfTheFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Sisters of the Flame produces one red mana")
    void tappingProducesRedMana() {
        addCreatureReady(player1, new SistersOfTheFlame());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability resolves immediately into its controller's pool")
    void manaAbilityResolvesImmediatelyForSecondPlayer() {
        Permanent permanent = addCreatureReady(player2, new SistersOfTheFlame());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Already-tapped Sisters of the Flame cannot produce mana again")
    void alreadyTappedCannotTapAgain() {
        Permanent permanent = addCreatureReady(player1, new SistersOfTheFlame());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Sisters of the Flame cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new SistersOfTheFlame());
        permanent.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(permanent.isTapped()).isFalse();
    }
}
