package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SeaScryer.class)
class SeaScryerTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapsForColorless() {
        Permanent scryer = addCreatureReady(player1, new SeaScryer());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(scryer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{1}, {T}: Add {U} consumes the generic mana and adds blue")
    void tapsForBlueAfterPayingGeneric() {
        Permanent scryer = addCreatureReady(player1, new SeaScryer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mana(ManaColor.COLORLESS)).isZero();
        assertThat(scryer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The blue ability can't be activated without mana to pay {1}")
    void blueAbilityRequiresManaPayment() {
        Permanent scryer = addCreatureReady(player1, new SeaScryer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mana(ManaColor.BLUE)).isZero();
        assertThat(scryer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Sea Scryer can't be tapped for mana")
    void summoningSickCannotTapForMana() {
        Permanent scryer = harness.addToBattlefieldAndReturn(player1, new SeaScryer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mana(ManaColor.COLORLESS)).isZero();
        assertThat(scryer.isTapped()).isFalse();
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
