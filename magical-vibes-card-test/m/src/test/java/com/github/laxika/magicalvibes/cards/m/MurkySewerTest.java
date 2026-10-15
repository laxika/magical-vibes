package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurkySewer.class})
class MurkySewerTest extends BaseCardTest {

    @Test
    void entersUntappedWhenControllerHas13Life() {
        harness.setLife(player1, 13);
        playSewer();

        assertThat(sewer().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHas13Life() {
        harness.setLife(player2, 13);
        playSewer();

        assertThat(sewer().isTapped()).isFalse();
    }

    @Test
    void entersTappedWhenNoPlayerHas13OrLessLife() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);
        playSewer();

        assertThat(sewer().isTapped()).isTrue();
    }

    @Test
    void entersUntappedWhenControllerHasLessThan13Life() {
        harness.setLife(player1, 12);
        playSewer();

        assertThat(sewer().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWhenOpponentHasLessThan13Life() {
        harness.setLife(player2, 12);
        playSewer();

        assertThat(sewer().isTapped()).isFalse();
    }

    @Test
    void entersTappedWhenPutOntoBattlefieldWithBothPlayersAbove13Life() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 14);

        Permanent sewer = harness.enterBattlefieldAndReturn(player1, new MurkySewer());

        assertThat(sewer.isTapped()).isTrue();
    }

    @Test
    void entersUntappedWhenPutOntoBattlefieldWithOpponentAt13Life() {
        harness.setLife(player2, 13);

        Permanent sewer = harness.enterBattlefieldAndReturn(player1, new MurkySewer());

        assertThat(sewer.isTapped()).isFalse();
    }

    @Test
    void tappingProducesBlueMana() {
        Permanent sewer = addReadySewer();

        harness.activateAbility(player1, 0, null, null);

        assertThat(sewer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void tappingProducesBlackMana() {
        Permanent sewer = addReadySewer();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(sewer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void playSewer() {
        harness.setHand(player1, List.of(new MurkySewer()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadySewer() {
        return addCreatureReady(player1, new MurkySewer());
    }

    private Permanent sewer() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
