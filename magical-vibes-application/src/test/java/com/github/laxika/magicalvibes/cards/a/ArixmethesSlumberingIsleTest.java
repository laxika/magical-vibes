package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArixmethesSlumberingIsle.class, GrizzlyBears.class})
class ArixmethesSlumberingIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with five slumber counters and is a land instead of a creature")
    void entersTappedWithSlumberCounters() {
        Permanent arixmethes = harness.enterBattlefieldAndReturn(player1, new ArixmethesSlumberingIsle());

        assertThat(arixmethes.isTapped()).isTrue();
        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(5);
        assertThat(gqs.isLand(gd, arixmethes)).isTrue();
        assertThat(gqs.isCreature(gd, arixmethes)).isFalse();
    }

    @Test
    @DisplayName("May remove a slumber counter when its controller casts a spell")
    void mayRemoveSlumberCounter() {
        Permanent arixmethes = addCreatureReady(player1, new ArixmethesSlumberingIsle());
        arixmethes.setCounterCount(CounterType.SLUMBER, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isZero();
        assertThat(gqs.isLand(gd, arixmethes)).isFalse();
        assertThat(gqs.isCreature(gd, arixmethes)).isTrue();
    }

    @Test
    @DisplayName("May decline removing a slumber counter")
    void mayDeclineRemovingSlumberCounter() {
        Permanent arixmethes = addCreatureReady(player1, new ArixmethesSlumberingIsle());
        arixmethes.setCounterCount(CounterType.SLUMBER, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(1);
        assertThat(gqs.isLand(gd, arixmethes)).isTrue();
        assertThat(gqs.isCreature(gd, arixmethes)).isFalse();
    }

    @Test
    @DisplayName("Tapping it adds one green and one blue mana")
    void tappingAddsGreenAndBlueMana() {
        Permanent arixmethes = addCreatureReady(player1, new ArixmethesSlumberingIsle());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(arixmethes.isTapped()).isTrue();
    }
}
