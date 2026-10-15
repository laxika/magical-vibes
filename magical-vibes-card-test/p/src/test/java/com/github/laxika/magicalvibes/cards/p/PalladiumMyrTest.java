package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PalladiumMyr.class})
class PalladiumMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Palladium Myr produces two colorless mana")
    void tappingProducesTwoColorlessMana() {
        addCreatureReady(player1, new PalladiumMyr());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Palladium Myr cannot tap for mana")
    void summoningSickCannotTap() {
        harness.addToBattlefield(player1, new PalladiumMyr());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped Palladium Myr cannot produce more mana")
    void alreadyTappedCannotTapAgain() {
        addCreatureReady(player1, new PalladiumMyr());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Palladium Myr's mana ability resolves immediately for its controller")
    void manaAbilityResolvesImmediatelyForController() {
        addCreatureReady(player2, new PalladiumMyr());

        harness.tapPermanent(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Palladium Myr can produce mana again after untapping")
    void canTapAgainAfterUntapping() {
        harness.addToBattlefield(player1, new PalladiumMyr());
        harness.performUntapStep(player1);
        harness.tapPermanent(player1, 0);

        harness.performUntapStep(player1);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
