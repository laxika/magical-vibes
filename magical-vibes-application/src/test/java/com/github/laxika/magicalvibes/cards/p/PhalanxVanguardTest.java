package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhalanxVanguard.class, Ornithopter.class, GrizzlyBears.class})
class PhalanxVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when an artifact you control enters")
    void getsBoostWhenOwnArtifactEnters() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new PhalanxVanguard());

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new PhalanxVanguard());

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(harness.getGameData(), vanguard)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(harness.getGameData(), vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's artifact entering does not trigger it")
    void opponentArtifactDoesNotTrigger() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new PhalanxVanguard());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-artifact entering does not trigger it")
    void nonArtifactDoesNotTrigger() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new PhalanxVanguard());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(harness.getGameData(), vanguard)).isEqualTo(2);
    }

    @Test
    void multipleArtifactEntriesGiveSeparateCumulativeBoosts() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new PhalanxVanguard());
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    void vigilanceKeepsVanguardUntappedWhenAttacking() {
        Permanent vanguard = addCreatureReady(player1, new PhalanxVanguard());

        declareAttackers(List.of(0));

        assertThat(vanguard.isTapped()).isFalse();
    }

    @Test
    @CardUsed({MycosynthLattice.class})
    void enteringAsAnArtifactTriggersItsOwnAbility() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new PhalanxVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent vanguard = findPermanent(player1, "Phalanx Vanguard");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }
}
