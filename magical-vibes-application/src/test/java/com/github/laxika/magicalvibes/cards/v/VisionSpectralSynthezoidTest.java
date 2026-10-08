package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.u.UltronDrone;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionSpectralSynthezoid.class, Divination.class, UltronDrone.class, GrizzlyBears.class,
        NightsWhisper.class, SwordsToPlowshares.class, SwiftfootBoots.class})
class VisionSpectralSynthezoidTest extends BaseCardTest {

    @Test
    void castsNoncreatureSpellFromHandForFreeOnceDuringControllerTurn() {
        harness.addToBattlefield(player1, new VisionSpectralSynthezoid());
        harness.setHand(player1, List.of(new Divination()));

        harness.castSorcery(player1, 0);
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

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseFreeInstantCastDuringOpponentsTurn() {
        var vision = harness.addToBattlefieldAndReturn(player1, new VisionSpectralSynthezoid());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, vision.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGrantFreeSpellsToOpponent() {
        var vision = harness.addToBattlefieldAndReturn(player1, new VisionSpectralSynthezoid());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, vision.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void freeCastAllowanceRenewsOnNextControllerTurn() {
        harness.addToBattlefield(player1, new VisionSpectralSynthezoid());
        harness.setHand(player1, List.of(new SwiftfootBoots()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Swiftfoot Boots");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Night's Whisper");
        harness.assertLife(player1, 18);
    }

    @Test
    void noncreatureAndRobotSpellsShareOneFreeCastAllowance() {
        harness.addToBattlefield(player1, new VisionSpectralSynthezoid());
        harness.setHand(player1, List.of(new SwiftfootBoots(), new UltronDrone()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
