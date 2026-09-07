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

@CardUsed({AncientImperiosaur.class, GrizzlyBears.class})
class AncientImperiosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters for each creature that convoked it")
    void entersWithCountersForConvokeCreatures() {
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncientImperiosaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId(), thirdConvokeCreature.getId()));
        harness.passBothPriorities();

        Permanent imperiosaur = findPermanent(player1, "Ancient Imperiosaur");
        assertThat(imperiosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();
        assertThat(thirdConvokeCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters without counters when no creature convoked it")
    void entersWithoutCountersWhenNotConvoked() {
        harness.setHand(player1, List.of(new AncientImperiosaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent imperiosaur = findPermanent(player1, "Ancient Imperiosaur");
        assertThat(imperiosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
