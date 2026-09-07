package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurnishedDunestomper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TarkirDuneshaper.class, BurnishedDunestomper.class})
class TarkirDuneshaperTest extends BaseCardTest {

    @Test
    void transformsByPayingGreenMana() {
        Permanent duneshaper = addDuneshaper();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(duneshaper.isTransformed()).isTrue();
        assertThat(duneshaper.getCard()).isInstanceOf(BurnishedDunestomper.class);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent duneshaper = addDuneshaper();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(duneshaper.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addDuneshaper();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addDuneshaper() {
        return harness.addToBattlefieldAndReturn(player1, new TarkirDuneshaper());
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
