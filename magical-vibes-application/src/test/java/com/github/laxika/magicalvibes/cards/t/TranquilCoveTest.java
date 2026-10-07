package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TranquilCove.class})
class TranquilCoveTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield tapped gains 1 life")
    void entersTappedAndGainsOneLife() {
        harness.setHand(player1, List.of(new TranquilCove()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent cove = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cove.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        Permanent cove = harness.addToBattlefieldAndReturn(player1, new TranquilCove());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(cove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent cove = harness.addToBattlefieldAndReturn(player1, new TranquilCove());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(cove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain uses the stack and survives its source leaving")
    void lifeGainResolvesAfterSourceLeaves() {
        harness.setHand(player1, List.of(new TranquilCove()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opposing controller gains life when playing the land")
    void opposingControllerGainsLife() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TranquilCove()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
    }
}
