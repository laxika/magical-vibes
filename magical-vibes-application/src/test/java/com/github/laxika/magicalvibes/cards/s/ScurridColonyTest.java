package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScurridColonyTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 2/2 below eight lands")
    void noBonusBelowEightLands() {
        addLands(player1, 7);
        harness.addToBattlefield(player1, new ScurridColony());

        Permanent colony = findColony();
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 at eight lands")
    void getsBonusAtEightLands() {
        addLands(player1, 8);
        harness.addToBattlefield(player1, new ScurridColony());

        Permanent colony = findColony();
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses the bonus when the controller drops below eight lands")
    void losesBonusWhenLandsDrop() {
        addLands(player1, 8);
        harness.addToBattlefield(player1, new ScurridColony());

        Permanent colony = findColony();
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's lands do not count")
    void opponentsLandsDoNotCount() {
        addLands(player2, 8);
        harness.addToBattlefield(player1, new ScurridColony());

        Permanent colony = findColony();
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(2);
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private Permanent findColony() {
        return findPermanent(player1, "Scurrid Colony");
    }
}
