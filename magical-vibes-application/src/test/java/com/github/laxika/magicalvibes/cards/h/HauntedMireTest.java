package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed(HauntedMire.class)
class HauntedMireTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new HauntedMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Haunted Mire").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces black mana")
    void tappingProducesBlackMana() {
        addReadyMire(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping produces green mana")
    void tappingProducesGreenMana() {
        addReadyMire(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addReadyMire(Player player) {
        Permanent permanent = new Permanent(new HauntedMire());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
