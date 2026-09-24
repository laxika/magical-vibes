package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WoodedRidgeline.class)
class WoodedRidgelineTest extends BaseCardTest {

    @Test
    @DisplayName("Wooded Ridgeline enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new WoodedRidgeline()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Wooded Ridgeline").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Wooded Ridgeline taps for red mana")
    void tapsForRedMana() {
        tapFor(ManaColor.RED);
    }

    @Test
    @DisplayName("Wooded Ridgeline taps for green mana")
    void tapsForGreenMana() {
        tapFor(ManaColor.GREEN);
    }

    private void tapFor(ManaColor color) {
        Permanent ridgeline = addReadyRidgeline(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(ridgeline.isTapped()).isTrue();
    }

    private Permanent addReadyRidgeline(Player player) {
        Permanent permanent = new Permanent(new WoodedRidgeline());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
