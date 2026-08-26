package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PlatedKilnbeast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        BondedHerdbeast.class,
        PlatedKilnbeast.class
})
class BondedHerdbeastTest extends BaseCardTest {

    @Test
    void transformsByPayingRedMana() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(herdbeast.isTransformed()).isTrue();
        assertThat(herdbeast.getCard()).isInstanceOf(PlatedKilnbeast.class);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(herdbeast.isTransformed()).isTrue();
        assertThat(herdbeast.getCard()).isInstanceOf(PlatedKilnbeast.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private Permanent addHerdbeast() {
        return harness.addToBattlefieldAndReturn(player1, new BondedHerdbeast());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
