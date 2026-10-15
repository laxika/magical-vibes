package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Scrubland.class)
class ScrublandTest extends BaseCardTest {

    @Test
    @DisplayName("Scrubland produces white mana")
    void producesWhiteMana() {
        Permanent scrubland = addScrublandReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(scrubland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Scrubland produces black mana")
    void producesBlackMana() {
        Permanent scrubland = addScrublandReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(scrubland.isTapped()).isTrue();
    }

    private Permanent addScrublandReady() {
        Permanent scrubland = harness.addToBattlefieldAndReturn(player1, new Scrubland());
        scrubland.setSummoningSick(false);
        return scrubland;
    }

    @Test
    @DisplayName("Scrubland can produce mana immediately after entering the battlefield")
    void producesManaImmediatelyAfterEntering() {
        Permanent scrubland = harness.enterBattlefieldAndReturn(player1, new Scrubland());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(scrubland.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Producing one color prevents producing the other without untapping")
    void cannotProduceBothColorsFromOneTap() {
        addScrublandReady();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
