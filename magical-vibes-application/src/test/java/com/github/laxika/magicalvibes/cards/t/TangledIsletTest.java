package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TangledIslet.class)
class TangledIsletTest extends BaseCardTest {

    @Test
    @DisplayName("Tangled Islet enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new TangledIslet()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Tangled Islet").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangled Islet taps for green mana")
    void tapsForGreenMana() {
        tapFor(ManaColor.GREEN);
    }

    @Test
    @DisplayName("Tangled Islet taps for blue mana")
    void tapsForBlueMana() {
        tapFor(ManaColor.BLUE);
    }

    private void tapFor(ManaColor color) {
        Permanent islet = addReadyIslet(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(islet.isTapped()).isTrue();
    }

    private Permanent addReadyIslet(Player player) {
        Permanent permanent = new Permanent(new TangledIslet());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
