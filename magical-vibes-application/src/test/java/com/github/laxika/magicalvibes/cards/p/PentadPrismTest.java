package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PentadPrismTest extends BaseCardTest {

    @Test
    void sunburstPutsOneChargeCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new PentadPrism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent prism = findPermanent(player1, "Pentad Prism");
        assertThat(prism.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void removesChargeCounterAndAddsOneManaOfChosenColor() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new PentadPrism());
        prism.setCounterCount(CounterType.CHARGE, 1);
        GameData gd = harness.getGameData();
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        int before = pool.get(ManaColor.RED);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(prism.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(pool.get(ManaColor.RED)).isEqualTo(before + 1);
    }

    @Test
    void cannotActivateWithoutChargeCounter() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new PentadPrism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
