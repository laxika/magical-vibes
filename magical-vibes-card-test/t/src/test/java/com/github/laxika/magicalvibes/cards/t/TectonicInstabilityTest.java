package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TectonicInstability.class, Forest.class, Island.class, IvoryMask.class, RagingKavu.class})
class TectonicInstabilityTest extends BaseCardTest {

    @Test
    @DisplayName("A land entering under your control taps all your lands but not other permanents")
    void ownLandTapsControlledLands() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TectonicInstability());
        Permanent existingLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Island()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(existingLand.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        assertThat(source.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's land entering taps all lands that opponent controls")
    void opponentLandTapsOpponentsLands() {
        harness.addToBattlefield(player1, new TectonicInstability());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RagingKavu());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(opponentLand.isTapped()).isTrue();
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Shroud does not prevent tapping the lands of the entering land's controller")
    void opponentShroudDoesNotPreventTapping() {
        harness.addToBattlefield(player1, new TectonicInstability());
        harness.addToBattlefield(player2, new IvoryMask());
        Permanent existingLand = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(existingLand.isTapped()).isTrue();
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lands can produce mana in response and tapping the other lands produces no mana")
    void canFloatManaBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new TectonicInstability());
        Permanent existingLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Island()));
        harness.playLand(player1, 0);

        assertThat(existingLand.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Island").isTapped()).isFalse();
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(existingLand.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("A nonland entering does not trigger Tectonic Instability")
    void nonlandEntryDoesNotTapLands() {
        harness.addToBattlefield(player1, new TectonicInstability());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new RagingKavu(), "{1}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raging Kavu");
        assertThat(gd.stack).isEmpty();
        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();
    }
}
