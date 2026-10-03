package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.h.Hundroog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BerserkMurlodont.class, Hundroog.class, AvenEnvoy.class})
class BerserkMurlodontTest extends BaseCardTest {

    @Test
    @DisplayName("A blocked Beast gets +1/+1 for each creature blocking it")
    void blockedBeastGetsBonusForEachBlocker() {
        Permanent murlodont = addCreatureReady(player1, new BerserkMurlodont());
        addCreatureReady(player2, new AvenEnvoy());
        addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(murlodont.getPowerModifier()).isEqualTo(2);
        assertThat(murlodont.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A blocked Beast controlled by an opponent also gets the bonus")
    void blockedOpponentBeastGetsBonus() {
        addCreatureReady(player1, new BerserkMurlodont());
        Permanent beast = addCreatureReady(player2, new Hundroog());
        addCreatureReady(player1, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(beast.getPowerModifier()).isEqualTo(1);
        assertThat(beast.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Berserk Murlodont does not trigger for a blocked non-Beast")
    void doesNotTriggerForBlockedNonBeast() {
        addCreatureReady(player1, new BerserkMurlodont());
        Permanent nonBeast = addCreatureReady(player1, new AvenEnvoy());
        addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(nonBeast.getPowerModifier()).isZero();
        assertThat(nonBeast.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An unblocked Beast does not get a bonus")
    void unblockedBeastDoesNotGetBonus() {
        Permanent murlodont = addCreatureReady(player1, new BerserkMurlodont());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(murlodont.getPowerModifier()).isZero();
        assertThat(murlodont.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The blocking bonus wears off at end of turn")
    void blockingBonusWearsOffAtEndOfTurn() {
        Permanent murlodont = addCreatureReady(player1, new BerserkMurlodont());
        addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(murlodont.getPowerModifier()).isEqualTo(1);
        assertThat(murlodont.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(murlodont.getPowerModifier()).isZero();
        assertThat(murlodont.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each Murlodont grants its bonus to another blocked Beast")
    void multipleMurlodontsBoostAnotherBeast() {
        Permanent first = addCreatureReady(player1, new BerserkMurlodont());
        Permanent second = addCreatureReady(player1, new BerserkMurlodont());
        Permanent beast = addCreatureReady(player1, new Hundroog());
        addCreatureReady(player2, new AvenEnvoy());
        addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(2));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 2),
                new BlockerAssignment(1, 2)
        ));
        resolveAllTriggers();

        assertThat(beast.getPowerModifier()).isEqualTo(4);
        assertThat(beast.getToughnessModifier()).isEqualTo(4);
        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each blocked Beast counts only its own blockers")
    void simultaneouslyBlockedBeastsCountTheirOwnBlockers() {
        Permanent murlodont = addCreatureReady(player1, new BerserkMurlodont());
        Permanent beast = addCreatureReady(player1, new Hundroog());
        addCreatureReady(player2, new AvenEnvoy());
        addCreatureReady(player2, new AvenEnvoy());
        addCreatureReady(player2, new AvenEnvoy());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1),
                new BlockerAssignment(2, 1)
        ));
        resolveAllTriggers();

        assertThat(murlodont.getPowerModifier()).isEqualTo(1);
        assertThat(murlodont.getToughnessModifier()).isEqualTo(1);
        assertThat(beast.getPowerModifier()).isEqualTo(2);
        assertThat(beast.getToughnessModifier()).isEqualTo(2);
    }
}
