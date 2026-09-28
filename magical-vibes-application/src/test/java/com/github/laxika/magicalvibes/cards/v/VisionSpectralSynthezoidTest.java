package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.u.UltronDrone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionSpectralSynthezoid.class, Divination.class, UltronDrone.class, GrizzlyBears.class})
class VisionSpectralSynthezoidTest extends BaseCardTest {

    @Test
    void castsNoncreatureSpellFromHandForFreeOnceDuringControllerTurn() {
        harness.addToBattlefield(player1, new VisionSpectralSynthezoid());
        harness.setHand(player1, List.of(new Divination()));

        harness.castSorcery(player1, 0, 0);
    }

    @Test
    void castsRobotCreatureSpellFromHandForFree() {
        harness.addToBattlefield(player1, new VisionSpectralSynthezoid());
        harness.setHand(player1, List.of(new UltronDrone()));

        harness.castCreature(player1, 0);
    }

    @Test
    void doesNotMakeNonRobotCreatureSpellFree() {
        harness.addToBattlefield(player1, new VisionSpectralSynthezoid());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allowsOnlyOneFreeCastDuringEachControllerTurn() {
        harness.addToBattlefield(player1, new VisionSpectralSynthezoid());
        harness.setHand(player1, List.of(new Divination(), new Divination()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
