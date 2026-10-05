package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EncroachingMycosynth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LevitatingStatue;
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

@CardUsed({PerimeterPatrol.class, Ornithopter.class, GrizzlyBears.class,
        EncroachingMycosynth.class, LevitatingStatue.class})
class PerimeterPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when an artifact you control enters")
    void getsBoostWhenOwnArtifactEnters() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new PerimeterPatrol());

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, patrol)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new PerimeterPatrol());

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(harness.getGameData(), patrol)).isEqualTo(4);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(harness.getGameData(), patrol)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's artifact entering does not trigger it")
    void opponentArtifactDoesNotTrigger() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new PerimeterPatrol());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-artifact entering does not trigger it")
    void nonArtifactDoesNotTrigger() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new PerimeterPatrol());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(harness.getGameData(), patrol)).isEqualTo(3);
    }

    @Test
    @DisplayName("Artifact entries create separate boosts that apply only after resolution")
    void multipleArtifactEntriesAccumulateResolvedBoosts() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new PerimeterPatrol());
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, patrol)).isEqualTo(3);
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
    }

    @Test
    @DisplayName("A noncreature artifact entering also triggers the boost")
    void noncreatureArtifactTriggersBoost() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new PerimeterPatrol());
        harness.setHand(player1, List.of(new LevitatingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, patrol)).isEqualTo(3);
    }

    @Test
    @DisplayName("Triggers for its own entry when Encroaching Mycosynth makes it an artifact")
    void triggersForOwnEntryWhenItIsAnArtifact() {
        harness.addToBattlefield(player1, new EncroachingMycosynth());
        harness.setHand(player1, List.of(new PerimeterPatrol()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent patrol = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Perimeter Patrol"));
        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, patrol)).isEqualTo(3);
    }
}
