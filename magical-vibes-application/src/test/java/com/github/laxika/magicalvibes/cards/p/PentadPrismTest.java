package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PentadPrism.class})
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
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new PentadPrism()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent prism = findPermanent(player1, "Pentad Prism");
        assertThat(prism.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void sunburstDoesNotCountColorlessMana() {
        harness.setHand(player1, List.of(new PentadPrism()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent prism = findPermanent(player1, "Pentad Prism");
        assertThat(prism.getCounterCount(CounterType.CHARGE)).isZero();
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
    void sunburstCountsColoredManaAlongsideColorlessMana() {
        harness.setHand(player1, List.of(new PentadPrism()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pentad Prism").getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void tappedPrismCanProduceEachColorRepeatedlyUntilCountersRunOut(ManaColor color) {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new PentadPrism());
        prism.setCounterCount(CounterType.CHARGE, 2);
        prism.setTapped(true);
        ManaPool pool = harness.getGameData().playerManaPools.get(player1.getId());
        int before = pool.get(color);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            assertThat(prism.getCounterCount(CounterType.CHARGE)).isEqualTo(1 - i);
            assertThat(harness.getGameData().stack).isEmpty();
            harness.handleListChoice(player1, color.name());
            assertThat(pool.get(color)).isEqualTo(before + i + 1);
        }

        assertThat(prism.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Pentad Prism");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutChargeCounter() {
        harness.addToBattlefield(player1, new PentadPrism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
