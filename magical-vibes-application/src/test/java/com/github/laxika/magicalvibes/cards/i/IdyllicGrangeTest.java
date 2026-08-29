package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IdyllicGrange.class, Plains.class, GrizzlyBears.class})
class IdyllicGrangeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with fewer than three other Plains and does not trigger")
    void entersTappedWithFewerThanThreeOtherPlains() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new IdyllicGrange()));

        harness.playLand(player1, 0);

        Permanent grange = findPermanent(player1, "Idyllic Grange");
        assertThat(grange.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters untapped with three other Plains and puts a +1/+1 counter on a creature you control")
    void entersUntappedWithThreeOtherPlainsAndPutsCounterOnControlledCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new IdyllicGrange()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Idyllic Grange").isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for mana adds one white mana")
    void tappingForManaAddsWhiteMana() {
        Permanent grange = harness.addToBattlefieldAndReturn(player1, new IdyllicGrange());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(grange.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
