package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkirkOutrider.class, AxebaneBeast.class})
class SkirkOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Is a 2/2 without trample when you do not control a Beast")
    void noBeast() {
        harness.addToBattlefield(player1, new SkirkOutrider());

        Permanent outrider = findPermanent(player1, "Skirk Outrider");
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +2/+2 and trample while you control a Beast")
    void boostWithBeast() {
        harness.addToBattlefield(player1, new SkirkOutrider());
        harness.addToBattlefield(player1, new AxebaneBeast());

        Permanent outrider = findPermanent(player1, "Skirk Outrider");
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Beast does not enable the ability")
    void opponentBeastDoesNotCount() {
        harness.addToBattlefield(player1, new SkirkOutrider());
        harness.addToBattlefield(player2, new AxebaneBeast());

        Permanent outrider = findPermanent(player1, "Skirk Outrider");
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when your Beast leaves the battlefield")
    void losesBoostWhenBeastLeaves() {
        harness.addToBattlefield(player1, new SkirkOutrider());
        harness.addToBattlefield(player1, new AxebaneBeast());

        Permanent outrider = findPermanent(player1, "Skirk Outrider");
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Axebane Beast"));

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.TRAMPLE)).isFalse();
    }
}
