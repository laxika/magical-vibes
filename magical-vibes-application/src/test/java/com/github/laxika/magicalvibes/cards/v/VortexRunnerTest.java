package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VortexRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the bonus below eight lands")
    void noBonusBelowEightLands() {
        addLands(player1, 7);
        harness.addToBattlefield(player1, new VortexRunner());

        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and can't be blocked at eight lands")
    void getsBonusAtEightLands() {
        addLands(player1, 8);
        harness.addToBattlefield(player1, new VortexRunner());

        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, runner)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when the controller drops below eight lands")
    void losesBonusWhenLandsDrop() {
        addLands(player1, 8);
        harness.addToBattlefield(player1, new VortexRunner());

        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();
    }

    @Test
    @DisplayName("Opponent's lands do not count")
    void opponentsLandsDoNotCount() {
        addLands(player2, 8);
        harness.addToBattlefield(player1, new VortexRunner());

        Permanent runner = findRunner();
        assertThat(gqs.getEffectivePower(gd, runner)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, runner)).isFalse();
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private Permanent findRunner() {
        return findPermanent(player1, "Vortex Runner");
    }
}
